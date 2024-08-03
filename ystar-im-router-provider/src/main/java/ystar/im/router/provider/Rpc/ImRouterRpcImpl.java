package ystar.im.router.provider.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.im.router.interfaces.ImRouterRpc;
import ystar.im.router.provider.service.ImRouterService;

@DubboService
public class ImRouterRpcImpl implements ImRouterRpc {
    
    @Resource
    private ImRouterService routerService;

    @Override
    public boolean sendMsg(Long objectId , String msgJson) {
        return false;
    }
}