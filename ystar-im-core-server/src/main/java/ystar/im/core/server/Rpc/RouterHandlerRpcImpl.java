package ystar.im.core.server.Rpc;

import org.apache.dubbo.config.annotation.DubboService;
import ystar.im.core.server.interfaces.IRouterHandlerRpc;

@DubboService
public class RouterHandlerRpcImpl implements IRouterHandlerRpc {

    @Override
    public void sendMsg(Long objectId , String msgJson) {
        System.out.println("this is im-core-server");
    }
}