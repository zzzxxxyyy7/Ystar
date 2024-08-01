package ystar.auth.account.Service.Impl;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystar.auth.account.Service.IAccountTokenService;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class AccountTokenServiceImpl implements IAccountTokenService {

    @Resource
    private RedisTemplate<String , String> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;
    
    @Override
    public String createAndSaveLoginToken(Long userId) {
        String LoginToken = UUID.randomUUID().toString();
        String redisKey = userProviderCacheKeyBuilder.buildUserLoginTokenKey(LoginToken);
        redisTemplate.opsForValue().set(redisKey , userId.toString() , 30 , TimeUnit.DAYS);
        System.out.println("构建 Redis 名称:" + redisKey);
        return LoginToken;
    }

    @Override
    public Long getUserIdByToken(String token) {
        String redisKey = userProviderCacheKeyBuilder.buildUserLoginTokenKey(token);
        String userId = redisTemplate.opsForValue().get(redisKey);
        System.out.println("获取到的 userId：" + userId);
        if (userId == null) return null;
        return Long.parseLong(userId);
    }
}