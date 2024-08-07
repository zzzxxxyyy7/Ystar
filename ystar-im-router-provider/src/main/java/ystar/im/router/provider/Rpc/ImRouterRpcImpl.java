package ystar.im.router.provider.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.router.interfaces.ImRouterRpc;
import ystar.im.router.provider.service.ImRouterService;

import java.util.List;

@DubboService
public class ImRouterRpcImpl implements ImRouterRpc {
    
    @Resource
    private ImRouterService routerService;

    @Override
    public boolean sendMsg(ImMsgBody imMsgBody) {
        return routerService.sendMsg(imMsgBody);
    }

    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodies) {
        routerService.batchSendMsg(imMsgBodies);
    }
}