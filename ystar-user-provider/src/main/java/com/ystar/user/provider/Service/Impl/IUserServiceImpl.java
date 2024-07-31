package com.ystar.user.provider.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.common.utils.ConvertBeanUtils;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.provider.Domain.mapper.IUserMapper;
import com.ystar.user.provider.Domain.po.UserPO;
import com.ystar.user.provider.Service.IUserService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class IUserServiceImpl extends ServiceImpl<IUserMapper, UserPO> implements IUserService {
    @Resource
    private IUserMapper iUserMapper;

    @Resource
    private RedisTemplate<String , UserDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private MQProducer mqProducer;

    @Override
    public UserDTO getUserById(Long userId) {
        if(userId == null) return null;

        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userId);

        UserDTO userDTO = redisTemplate.opsForValue().get(key);
        if (userDTO != null) return userDTO;

        userDTO = ConvertBeanUtils.convert(iUserMapper.selectById(userId) , UserDTO.class);
        if (userDTO != null) redisTemplate.opsForValue().set(key , userDTO , 30 , TimeUnit.MINUTES);

        return userDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if(userDTO == null || userDTO.getUserId() == null) {
            return false;
        }
        iUserMapper.updateById(ConvertBeanUtils.convert(userDTO , UserPO.class));

        // 更新完数据库，删除缓存
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());
        redisTemplate.delete(key);

        // TODO RocketMQ部署不上去 发送MQ，延迟双删
//        Message message = new Message();
//        // 设置消息内容
//        message.setBody(JSON.toJSONString(userDTO).getBytes());
//        // 绑定 Topic
//        message.setTopic("user-update-cache");
//        // 延迟级别、延迟一秒
//        message.setDelayTimeLevel(1);
//        // 发送消息
//        try {
//            mqProducer.send(message);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
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
        // TODO 缓存雪崩解决，针对没在缓存的去查库，缓存穿透还未解决
        if (CollectionUtils.isEmpty(userIdList)) return new HashMap<>();
//        userIdList = userIdList.stream().filter(id -> id > 10000).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(userIdList)) return new HashMap<>();

        List<String> multiKeyList = new ArrayList<>();
        userIdList.forEach(userId -> multiKeyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId)));
        // 缓存匹配，非空过滤
        List<UserDTO> userDTOList = redisTemplate.opsForValue().multiGet(multiKeyList).stream().filter(x -> x != null).collect(Collectors.toList());

        // 提速、提前判断
        if (!CollectionUtils.isEmpty(userDTOList) && userDTOList.size() == userIdList.size()) {
            return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId , userDTO -> userDTO));
        }

        // 在缓存中的集合
        List<Long> userIdInCacheList = userDTOList.stream().map(UserDTO::getUserId).toList();
        // 没在缓存的集合
        List<Long> userIdNotInCacheList = userIdList.stream().filter(x -> !userIdInCacheList.contains(x)).toList();

        /**
         * 缓存没有的再去查 DB
         * 并行流多线程处理 替换 union all 方式 提高执行效率
         */
        Map<Long, List<Long>> userIdMap = userIdNotInCacheList.stream().collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> dbQueryResult = new CopyOnWriteArrayList<>();
        // 并行流，所有的数据会被划分为不同的段
        userIdMap.values().parallelStream().forEach(queryUserIdList -> {
            dbQueryResult.addAll(ConvertBeanUtils.convertList(iUserMapper.selectBatchIds(queryUserIdList) , UserDTO.class));
        });

        if (!CollectionUtils.isEmpty(dbQueryResult)) {
            Map<String , UserDTO> saveCacheMap = dbQueryResult.stream().collect(
              Collectors.toMap(userDTO -> userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()) , x -> x)
            );
            redisTemplate.opsForValue().multiSet(saveCacheMap);

            // PipeLine 批量设置过期时间
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    // 针对每一个 key , 设置过期时间
                    for (String redisKey : saveCacheMap.keySet()) {
                        operations.expire((K) redisKey, createRandomExpireTime(), TimeUnit.SECONDS);
                    }
                    return null;
                }
            });

            userDTOList.addAll(dbQueryResult);
        }

        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, userDTO -> userDTO));
    }

    //生成随机过期时间，单位：秒
    private long createRandomExpireTime() {
        return ThreadLocalRandom.current().nextLong(1000) + 60 * 30;//30min + 1000s
    }
}