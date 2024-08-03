package ystar.im.provider.Service.Impl;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystar.im.provider.Service.ImTokenService;
import ystart.framework.redis.starter.key.ImProviderCacheKeyBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ImTokenServiceImpl implements ImTokenService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ImProviderCacheKeyBuilder imProviderCacheKeyBuilder;

    @Override
    public String createImLoginToken(Long userId, int appId) {
        String token = UUID.randomUUID() + "%" + appId;
        redisTemplate.opsForValue().set(imProviderCacheKeyBuilder.buildImLoginTokenKey(token), userId.toString(), 5L, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public Long getUserIdByToken(String token) {
        String userId = redisTemplate.opsForValue().get(imProviderCacheKeyBuilder.buildImLoginTokenKey(token));
        if (userId == null) return null;
        return Long.parseLong(userId);
    }
}