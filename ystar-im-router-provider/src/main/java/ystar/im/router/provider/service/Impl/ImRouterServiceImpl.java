package ystar.im.router.provider.service.Impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Service;
import ystar.im.core.server.interfaces.IRouterHandlerRpc;
import ystar.im.router.provider.service.ImRouterService;

@Service
public class ImRouterServiceImpl implements ImRouterService {

    @DubboReference
    private IRouterHandlerRpc iRouterHandlerRpc;

    @Override
    public boolean sendMsg(Long objectId , String msgJson) {

        String objectImServerIp = "192.168.202.1:9095"; //core-server的ip地址+routerHandlerRpc调用的端口
        RpcContext.getContext().set("ip" , objectImServerIp);

        /**
         * 调用 im-core-server 服务，根据 Dubbo Cluster 的 Join 回调和 Invoker 方法来选择调用哪一个 机器服务
         * 根据 Dubbo 上下文传入的 IP 参数
         */
        iRouterHandlerRpc.sendMsg(objectId , msgJson);
        return false;
    }
}
