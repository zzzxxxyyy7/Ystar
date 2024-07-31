package com.ystar.user.provider.Rpc;

import com.ystar.user.constant.UserTagsEnum;
import com.ystar.user.interfaces.IUserTagRpc;
import com.ystar.user.provider.Service.IUserTagService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * User Tag 服务调用
 */
@DubboService
public class UserTagRpcImpl implements IUserTagRpc {

    @Resource
    private IUserTagService iUserTagService;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return iUserTagService.setTag(userId , userTagsEnum);
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return iUserTagService.cancelTag(userId , userTagsEnum);
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        return iUserTagService.containTag(userId , userTagsEnum);
    }
}
