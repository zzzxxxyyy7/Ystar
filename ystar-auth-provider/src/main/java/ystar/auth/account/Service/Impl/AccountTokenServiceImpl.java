package ystar.auth.account.Service.Impl;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ystar.auth.account.Service.IAccountTokenService;
import ystar.auth.account.Utils.JwtUtils;
import ystart.framework.redis.starter.key.AccountProviderCacheKeyBuilder;

import java.util.concurrent.TimeUnit;

@Service
public class AccountTokenServiceImpl implements IAccountTokenService {
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private AccountProviderCacheKeyBuilder cacheKeyBuilder;
    
    @Override
    public String createAndSaveLoginToken(Long userId) {
        String token = JwtUtils.generateToken(userId);
        stringRedisTemplate.opsForValue().set(cacheKeyBuilder.buildUserLoginTokenKey(token), userId.toString(), 30L, TimeUnit.DAYS);
        return token;
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        String userIdStr = stringRedisTemplate.opsForValue().get(cacheKeyBuilder.buildUserLoginTokenKey(tokenKey));
        if(StringUtils.isEmpty(userIdStr)) {
            return null;
        }
        return Long.valueOf(userIdStr);
    }
}