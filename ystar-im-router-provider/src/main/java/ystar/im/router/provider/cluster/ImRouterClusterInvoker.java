package ystar.im.router.provider.cluster;

import io.micrometer.common.util.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;

import java.util.List;

/**
 * Dubbo 在做节点选择的时候，按照我们给定的逻辑来进行节点选取
 */
public class ImRouterClusterInvoker<T> extends AbstractClusterInvoker<T> {

    public ImRouterClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    protected Result doInvoke(Invocation invocation, List list, LoadBalance loadbalance) throws RpcException {

        // 简单校验
        checkWhetherDestroyed();

        // 从上下文获取 ip
        String ip = RpcContext.getContext().get("ip").toString();
        if (StringUtils.isEmpty(ip)) throw new RuntimeException("ip can not be null");

        /**
         * Dubbo 底层在选择服务对象的时候，会把每个对象封装成 Invoker
         */
        List<Invoker<T>> invokers = list(invocation);
        Invoker<T> matchInvoker = invokers.stream().filter(invoker -> {
            // 拿到每一个服务提供者的暴露地址， IP + 端口 格式
            String serverIp = invoker.getUrl().getHost() + ":" + invoker.getUrl().getPort();
            return serverIp.equals(ip);
        }).findFirst().orElse(null);

        if (matchInvoker == null) {
            throw new RuntimeException("当前传入 IP 异常 ， 可能是 IP 错误或者服务已下线");
        }

        return matchInvoker.invoke(invocation);
    }
}
