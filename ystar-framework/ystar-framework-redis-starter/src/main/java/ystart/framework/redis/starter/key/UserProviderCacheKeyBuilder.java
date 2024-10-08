package ystart.framework.redis.starter.key;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Conditional;

@Configurable
@Conditional(RedisKeyLoadMatch.class)
public class UserProviderCacheKeyBuilder extends RedisKeyBuilder{

    private static final String USER_INFO_KEY = "userInfo";
    private static final String USER_TAG_LOCK_KEY = "userTagLock";
    private static final String USER_TAG_KEY = "userTag";
    private static final String USER_PHONE_LIST = "userPhoneList";
    private static final String USER_PHONE_OBJ = "userPhoneObj";
    private static final String USER_LOGIN_TOKEN = "userLoginToken";

    public String buildUserInfoKey(Long userId) {
        return super.getPrefix() + USER_INFO_KEY + super.getSplitItem() + userId;
    }

    public String buildUserTagLockKey(Long userId) {
        return super.getPrefix() + USER_TAG_LOCK_KEY + super.getSplitItem() + userId;
    }

    public String buildUserTagKey(Long userId) {
        return super.getPrefix() + USER_TAG_KEY + super.getSplitItem() + userId;
    }

    public String  buildUserPhoneListKey(Long userId) {
        return super.getPrefix() + USER_PHONE_LIST + super.getSplitItem() + userId;
    }

    public String buildUserPhoneObjKey(String phone) {
        return super.getPrefix() + USER_PHONE_OBJ + super.getSplitItem() + phone;
    }

    public String buildUserLoginTokenKey(String Token) {
        return super.getPrefix() + USER_LOGIN_TOKEN + super.getSplitItem() + Token;
    }
}