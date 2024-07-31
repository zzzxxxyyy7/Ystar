package ystart.framework.redis.starter.key;


import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Conditional;

@Configurable
@Conditional(RedisKeyLoadMatch.class)
public class MsgCacheKeyBuilder extends RedisKeyBuilder {
    private static final String MSG_LOGIN_INFO = "MsgLoginInfo";
    private static final String MSG_LOGIN_LOCK = "MsgLoginLock";
    private static final String MSG_LOGIN_TIMES = "MsgLoginTimes";

    public String buildMsgLoginInfoKey(String phone) {
        return super.getPrefix() + MSG_LOGIN_INFO + super.getSplitItem() + phone;
    }

    public String buildMsgLoginLockInfoKey(String phone) {
        return super.getPrefix() + MSG_LOGIN_LOCK + super.getSplitItem() + phone;
    }

    public String buildMsgLoginTimesInfoKey(String phone) {
        return super.getPrefix() + MSG_LOGIN_TIMES + super.getSplitItem() + phone;
    }
}
