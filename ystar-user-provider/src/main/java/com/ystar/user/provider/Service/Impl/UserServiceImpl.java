package com.ystar.user.provider.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.common.utils.ConvertBeanUtils;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.provider.Domain.mapper.IUserMapper;
import com.ystar.user.provider.Domain.po.UserPO;
import com.ystar.user.provider.Service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

@Service
public class UserServiceImpl extends ServiceImpl<IUserMapper, UserPO> implements IUserService {
    @Resource
    private IUserMapper iUserMapper;

    @Resource
    private RedisTemplate<String , UserDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Override
    public UserDTO getUserById(Long userId) {
        if(userId == null) return null;

        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userId);

        UserDTO userDTO = redisTemplate.opsForValue().get(key);
        if (userDTO != null) return userDTO;

        userDTO = ConvertBeanUtils.convert(iUserMapper.selectById(userId) , UserDTO.class);
        if (userDTO != null) redisTemplate.opsForValue().set(key , userDTO);

        return userDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if(userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        iUserMapper.updateById(ConvertBeanUtils.convert(userDTO , UserPO.class));
        return true;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        if(userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        iUserMapper.insert(ConvertBeanUtils.convert(userDTO, UserPO.class));
        return true;
    }
}