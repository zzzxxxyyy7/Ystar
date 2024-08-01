package ystart.framework.redis.starter.key;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Conditional;

@Configurable
@Conditional(RedisKeyLoadMatch.class)
public class AccountProviderCacheKeyBuilder extends RedisKeyBuilder {

    private static final String ACCOUNT_USER_LOGIN_TOKEN = "AccountUserLoginToken";

    public String buildUserLoginTokenKey(String phone) {
        return super.getPrefix() + ACCOUNT_USER_LOGIN_TOKEN + super.getSplitItem() + phone;
    }
}
