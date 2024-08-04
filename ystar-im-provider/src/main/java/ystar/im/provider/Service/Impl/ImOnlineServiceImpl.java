package ystar.im.provider.Service.Impl;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystar.im.constant.ImConstants;
import ystar.im.core.server.constants.ImCoreServerConstants;
import ystar.im.provider.Service.ImOnlineService;

import java.time.temporal.ValueRange;
import java.util.concurrent.TimeUnit;

/**
 * 避免 Im Core Server 负载过高，分担 Im Core Server 压力
 */
@Service
public class ImOnlineServiceImpl implements ImOnlineService {

    @Resource
    private RedisTemplate<String , String> redisTemplate;

    @Override
    public boolean isOnline(Long userId, int appId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId));
    }

}
