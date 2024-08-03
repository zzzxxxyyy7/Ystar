package ystart.framework.redis.starter.key;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(RedisKeyLoadMatch.class)
public class ImCoreServerProviderCacheKeyBuilder extends RedisKeyBuilder {

    /**
     * 心跳检测是否在线
     */
    private static String IM_ONLINE_ZSET = "imOnlineZset";

    /**
     * 在线分发消息的 ACK 机制
     */
    private static String IM_ACK_MAP = "imAckMap";

    /**
     * ACK 集合
     * @param userId
     * @param appId
     * @return
     */
    public String buildImAckMapKey(Long userId,Integer appId) {
        return super.getPrefix() + IM_ACK_MAP + super.getSplitItem() + appId + super.getSplitItem() + userId % 100;
    }

    /**
     * 按照用户 id 取模 10000，得出具体缓存所在的 key 通过取模路由的方式来避免大 key 问题
     *
     * @param userId
     * @return
     */
    public String buildImLoginTokenKey(Long userId, Integer appId) {
        return super.getPrefix() + IM_ONLINE_ZSET + super.getSplitItem() + appId + super.getSplitItem() + userId % 10000;
    }

}