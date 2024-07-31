package com.ystar.user.provider.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.common.utils.ConvertBeanUtils;
import com.ystar.user.constant.UserTagsEnum;
import com.ystar.user.dto.UserTagDTO;
import com.ystar.user.provider.Domain.mapper.TUserTagMapper;
import com.ystar.user.provider.Domain.po.UserTagPO;
import com.ystar.user.provider.Service.IUserTagService;
import com.ystar.user.provider.Utils.TagFactors;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

import java.util.concurrent.TimeUnit;

@Service
public class IUserTagServiceImpl extends ServiceImpl<TUserTagMapper, UserTagPO> implements IUserTagService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IUserTagServiceImpl.class);

    @Resource
    private TUserTagMapper tUserTagMapper;

    @Resource
    private TagFactors tagFactors;

    @Resource
    private RedisTemplate<String , UserTagDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        // 原先没有，现在有，设置成功，直接返回 True
        boolean updateStatus = tUserTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (updateStatus) {
            String userTagKey = userProviderCacheKeyBuilder.buildUserTagKey(userId);
            redisTemplate.delete(userTagKey);
            return true;
        }

        // 判断是否有记录
        // 先判断，只需要走一次数据库，不需要走俩次缓存，否则就是俩次缓存 + 一次数据库
        UserTagPO tUserTagPO = tUserTagMapper.selectById(userId);
        /*
          失败原因有二
          一、原先就没有这个用户，下面需要初始化设置
          二、原先有用户，但是已经有了标签，直接返回 false，因为用户已经有了标签
         */
        if (tUserTagPO != null) return false;

        /*
          原先没有用户，全新插入
          在高并发情况下，多线程进入到这一步，重置 ID 插入会导致大量失败，占用大量数据库连接池
          使用 Redisson 构建分布式锁，针对单个 ID 只允许一个线程发送数据库请求，后续请求不再发送数据库
         */
        String UserTagLockKey = userProviderCacheKeyBuilder.buildUserTagLockKey(userId);
        RLock rLock = redissonClient.getLock(UserTagLockKey);

        try {
            boolean acquireResult = rLock.tryLock(1L, 2L, TimeUnit.SECONDS);
            if (acquireResult) {
                UserTagPO newTUserTagPO = new UserTagPO();
                newTUserTagPO.setUserId(userId);
                tUserTagMapper.insert(newTUserTagPO);
                // 必须先保留结果再释放锁
                updateStatus = tUserTagMapper.setTag(userId, userTagsEnum.getFieldName() , userTagsEnum.getTag()) > 0;
            } else return false; // 本身就不是第一个请求了，已经有一个线程发送数据库请求了，后续线程直接失败
        } catch (Exception e) {
            throw new RuntimeException("设置 Tag 获取 RLock 分布式锁失败");
        } finally {
            rLock.unlock();
        }

        // 必须先保留结果再释放锁，如果锁已经释放导致返回结果阻塞后续线程就会不断发送数据库请求
        return updateStatus;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        boolean cancelStatus = tUserTagMapper.cancelTag(userId , userTagsEnum.getFieldName() , userTagsEnum.getTag()) > 0;
        if (!cancelStatus) return false;
        // 双删判断
        int doubleAcquire = tUserTagMapper.cancelDoubleAcquire(userId);
        if (doubleAcquire > 0) LOGGER.info("用户标签双删触发，userId is {}" , userId);

        String userTagKey = userProviderCacheKeyBuilder.buildUserTagKey(userId);
        redisTemplate.delete(userTagKey);
        return true;
    }

    /**
     * 看这个人是否拥有指定标签
     * 每次都去库查这个人？直接查缓存是否已经有了这个人
     * @param userId
     * @param userTagsEnum
     * @return
     */
    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        // TODO 改进：同时判断是否拥有多个标签
        UserTagDTO userTagDTO = this.queryUserTagDTOByRedis(userId);
        if (userTagDTO == null) return false;

        // 通过标签工程取出对应标签
        String fieldName = userTagsEnum.getFieldName();
        Long tagInfo = tagFactors.getTagByFileName(userTagDTO , fieldName);

        // 调用包含判断函数
        return isContain(tagInfo , userTagsEnum.getTag());
    }

    public static boolean isContain(Long tagInfo , Long matchTag) {
        return tagInfo != null && matchTag != null && (tagInfo & matchTag) == matchTag;
    }

    public UserTagDTO queryUserTagDTOByRedis(Long userId) {
        // 查缓存
        UserTagDTO userTagDTO = redisTemplate.opsForValue().get(userProviderCacheKeyBuilder.buildUserTagKey(userId));
        // 存在，直接返回
        if (userTagDTO != null) return userTagDTO;

        // 不存在，查数据库
        UserTagPO tUserTagPO = tUserTagMapper.selectById(userId);
        // TODO 缓存穿透后续补全，数据库也不存在，返回空
        if (tUserTagPO == null) return null;

        // 数据库存在，返回对象，放入缓存
        UserTagDTO convert = ConvertBeanUtils.convert(tUserTagPO, UserTagDTO.class);
        redisTemplate.opsForValue().set(userProviderCacheKeyBuilder.buildUserTagKey(userId) , convert , 30 , TimeUnit.MINUTES);
        return convert;
    }
}
