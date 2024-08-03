package ystar.im.router.provider.cluster;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Cluster;
import org.apache.dubbo.rpc.cluster.Directory;

/**
 * 基于Cluster去做spi扩展，实现根据rpc上下文来选择具体请求的机器
 */
public class ImRouterCluster implements Cluster {

    /**
     * Dubbo 在做集群服务筛选的时候，会有一个 join 方法的回调
     * @param directory
     * @param buildFilterChain
     * @return
     * @param <T>
     * @throws RpcException
     */
    @Override
    public <T> Invoker<T> join(Directory<T> directory, boolean buildFilterChain) throws RpcException {
        return new ImRouterClusterInvoker<>(directory);
    }

}
