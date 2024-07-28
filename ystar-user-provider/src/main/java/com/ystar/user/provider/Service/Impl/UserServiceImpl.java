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
import org.springframework.util.CollectionUtils;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) return new HashMap<>();
        // userIdList = userIdList.stream().filter(id -> id > 10000).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIdList)) return new HashMap<>();

        List<String> keyList = new ArrayList<>();
        userIdList.forEach(userId -> {
            keyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId));

        });
        // 缓存匹配，非空过滤
        List<UserDTO> userDTOS = redisTemplate.opsForValue().multiGet(keyList).stream().filter(x -> x != null).toList();

        // 提速、提前判断
        if (!CollectionUtils.isEmpty(userDTOS) && userDTOS.size() == userIdList.size()) {
            return userDTOS.stream().collect(Collectors.toMap(UserDTO::getUserId , userDTO -> userDTO));
        }

        // 在缓存中的集合
        List<Long> userIdInCacheList = userDTOS.stream().map(UserDTO::getUserId).toList();
        // 没在缓存的集合
        List<Long> userIdNotInCacheList = userIdList.stream().filter(x -> !userIdInCacheList.contains(x)).toList();

        /**
         * 缓存没有的再去查 DB
         * 并行流多线程处理 替换 union all 方式 提高执行效率
         */
        Map<Long, List<Long>> collect = userIdNotInCacheList.stream().collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> dbQueryResult = new CopyOnWriteArrayList<>();
        // 并行流，所有的数据会被划分为不同的段
        collect.values().parallelStream().forEach(queryUserIdList -> {
            dbQueryResult.addAll(ConvertBeanUtils.convertList(iUserMapper.selectBatchIds(queryUserIdList) , UserDTO.class));
        });

        if (!CollectionUtils.isEmpty(dbQueryResult)) {
            Map<String , UserDTO> saveCacheMap = dbQueryResult.stream().collect(
              Collectors.toMap(userDTO -> userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()) , x -> x)
            );
            redisTemplate.opsForValue().multiSet(saveCacheMap);
        }

        return dbQueryResult.stream().collect(Collectors.toMap(UserDTO::getUserId, userDTO -> userDTO));
    }
}