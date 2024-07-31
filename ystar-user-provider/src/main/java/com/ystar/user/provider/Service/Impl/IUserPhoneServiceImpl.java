package com.ystar.user.provider.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.common.utils.CommonStatusEnum;
import com.ystar.common.utils.ConvertBeanUtils;
import com.ystar.id.generate.interfaces.IdGenerateRpc;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.dto.UserLoginDTO;
import com.ystar.user.dto.UserPhoneDTO;
import com.ystar.user.provider.Domain.po.TUserPhonePo;
import com.ystar.user.provider.Service.IUserService;
import com.ystar.user.provider.Service.IUserPhoneService;
import com.ystar.user.provider.Domain.mapper.IUserPhoneMapper;
import com.ystar.user.provider.Utils.DESUtils;
import com.ystar.user.provider.Utils.JwtUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ystart.framework.redis.starter.id.RedisSeqIdHelper;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Rhss
* @description 针对表【t_user_phone_00】的数据库操作Service实现
* @createDate 2024-07-31 20:21:40
*/
@Service
public class IUserPhoneServiceImpl extends ServiceImpl<IUserPhoneMapper, TUserPhonePo>
    implements IUserPhoneService {

    @Resource
    private IUserPhoneMapper iUserPhoneMapper;

    @Resource
    private RedisTemplate<String , Object> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private IUserService iUserService;

    @DubboReference
    private IdGenerateRpc idGenerateRpc;

    @Resource
    private RedisSeqIdHelper redisSeqIdHelper;

    private static final String SuccessPhoneFormat = "手机号格式正确";

    public String checkPhone(String phone) {
        if (StringUtils.isEmpty(phone)) return "手机号为空";
        if (phone.length() != 11) return "手机号格式不正确";
        for (int i = 0 ; i < phone.length() ; ++i) {
            if (!(phone.charAt(i) >= '0' && phone.charAt(i) <= '9')) return "手机号含有错误字符";
        }
        return SuccessPhoneFormat;
    }

    /**
     * 登录 + 注册接口
     * @param phone
     * @return
     */
    @Override
    public UserLoginDTO login(String phone) {
        String desc = checkPhone(phone);
        if (!SuccessPhoneFormat.equals(desc)) return UserLoginDTO.LoginError(desc);

        /**
         * 用户有没有注册过是查手机号
         */
        UserPhoneDTO userPhoneDTO = queryByPhone(phone);

        // 注册过，刷新 Token 直接登录
        if (userPhoneDTO != null) {
            return UserLoginDTO.LoginSuccess(userPhoneDTO.getUserId() , JwtUtils.generateToken(userPhoneDTO));
        }

        // 没有注册过，就注册
        return registerAndLogin(phone);
    }

    @Override
    public UserPhoneDTO queryByPhone(String phone) {
        String redisKey = userProviderCacheKeyBuilder.buildUserPhoneObjKey(phone);
        UserPhoneDTO userPhoneDTO = (UserPhoneDTO) redisTemplate.opsForValue().get(redisKey);
        if (userPhoneDTO != null) {
            if (userPhoneDTO.getUserId() == null) {// 缓存穿透校验
                return null;
            }
            return userPhoneDTO;
        }
        // 没有缓存，从数据库查询
        userPhoneDTO = this.queryTUserPhoneFromDB(phone);
        if (userPhoneDTO != null) {
            userPhoneDTO.setPhone(DESUtils.decrypt(userPhoneDTO.getPhone()));
            redisTemplate.opsForValue().set(redisKey, userPhoneDTO, 30L, TimeUnit.MINUTES);
            return userPhoneDTO;
        }
        // 缓存穿透：缓存空对象
        redisTemplate.opsForValue().set(redisKey, new UserPhoneDTO(), 1L, TimeUnit.MINUTES);
        return null;
    }

    /**
     * 查数据库
     * @param phone
     * @return
     */
    public UserPhoneDTO queryTUserPhoneFromDB(String phone) {
        TUserPhonePo tUserPhonePo = iUserPhoneMapper.selectOne(
                new QueryWrapper<TUserPhonePo>()
                        .eq("phone", DESUtils.encrypt(phone))
                        .eq("status" , CommonStatusEnum.VALID_STATUS)
                        .last("limit 1"));
        String redisKey = userProviderCacheKeyBuilder.buildUserPhoneObjKey(phone);
        if (tUserPhonePo == null) {
            // TODO 缓存击穿
            redisTemplate.opsForValue().set(redisKey , new TUserPhonePo() , 30 , TimeUnit.MINUTES);
            return null;
        }
        redisTemplate.opsForValue().set(redisKey , tUserPhonePo, 5 , TimeUnit.MINUTES);
        return ConvertBeanUtils.convert(tUserPhonePo , UserPhoneDTO.class);
    }

    /**
     * 注册方法
     * @param phone
     * @return
     */
    /**
     * 注册新手机号用户
     *
     * @return
     */
    private UserLoginDTO registerAndLogin(String phone) {
        Long userId = idGenerateRpc.getSeqId(1);
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName("YStar用户-" + userId);
        // 插入用户表
        iUserService.insertOne(userDTO);
        TUserPhonePo userPhonePO = new TUserPhonePo();
        userPhonePO.setUserId(userId);
        userPhonePO.setPhone(DESUtils.encrypt(phone));
        userPhonePO.setStatus(CommonStatusEnum.VALID_STATUS.getCode());
        iUserPhoneMapper.insert(userPhonePO);
        // 需要删除空值对象，因为我们查询有无对应用户的时候，缓存了空对象，这里我们创建了就可以删除了
        redisTemplate.delete(userProviderCacheKeyBuilder.buildUserPhoneObjKey(phone));
        return UserLoginDTO.LoginSuccess(userId, JwtUtils.generateToken(ConvertBeanUtils.convert(userPhonePO , UserPhoneDTO.class)));
    }

    /**
     * 根据手机号查用户信息
     * @param userId
     * @return
     */
    @Override
    public List<UserPhoneDTO> queryByUserId(Long userId) {
        // 参数校验
        if (userId == null) {
            return Collections.emptyList();
        }
        String redisKey = userProviderCacheKeyBuilder.buildUserPhoneListKey(userId);
        List<Object> userPhoneList = redisTemplate.opsForList().range(redisKey, 0, -1);
        // Redis有缓存
        if (!CollectionUtils.isEmpty(userPhoneList)) {
            if (((UserPhoneDTO) userPhoneList.get(0)).getUserId() == null) {// 缓存穿透校验
                return Collections.emptyList();
            }
            return userPhoneList.stream().map(x -> (UserPhoneDTO) x).collect(Collectors.toList());
        }
        // 没有缓存，查询MySQL
        List<UserPhoneDTO> userPhoneDTOS = this.queryByUserIdFromDB(userId);
        if (!CollectionUtils.isEmpty(userPhoneDTOS)) {
            userPhoneDTOS.stream().forEach(x -> x.setPhone(DESUtils.decrypt(x.getPhone())));
            redisTemplate.opsForList().leftPushAll(redisKey, userPhoneDTOS.toArray());
            redisTemplate.expire(redisKey, 30L, TimeUnit.MINUTES);
            return userPhoneDTOS;
        }
        // 缓存穿透：缓存空对象
        redisTemplate.opsForList().leftPush(redisKey, new UserPhoneDTO());
        redisTemplate.expire(redisKey, 1L, TimeUnit.MINUTES);
        return Collections.emptyList();
    }

    private List<UserPhoneDTO> queryByUserIdFromDB(Long userId) {
        LambdaQueryWrapper<TUserPhonePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TUserPhonePo::getUserId, userId).eq(TUserPhonePo::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        return ConvertBeanUtils.convertList(iUserPhoneMapper.selectList(queryWrapper), UserPhoneDTO.class);
    }
}




