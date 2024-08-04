package ystar.im.core.server.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.core.server.interfaces.IRouterHandlerRpc;
import ystar.im.core.server.service.IRouterHandlerService;

@DubboService
public class RouterHandlerRpcImpl implements IRouterHandlerRpc {

    @Resource
    private IRouterHandlerService iRouterHandlerService;

    @Override
    public void sendMsg(ImMsgBody imMsgBody) {
        iRouterHandlerService.onReceive(imMsgBody);
    }
}