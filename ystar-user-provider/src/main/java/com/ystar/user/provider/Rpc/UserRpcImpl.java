package com.ystar.user.provider.Rpc;

import com.ystar.user.interfaces.IUserRpc;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserRpcImpl implements IUserRpc {
    @Override
    public String test() {
        System.out.println("Dubbo 调用成功");
        return "success";
    }
}
