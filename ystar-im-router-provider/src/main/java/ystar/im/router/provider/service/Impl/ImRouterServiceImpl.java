package ystar.im.router.provider.service.Impl;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.core.server.constants.ImCoreServerConstants;
import ystar.im.core.server.interfaces.IRouterHandlerRpc;
import ystar.im.router.provider.service.ImRouterService;

@Service
public class ImRouterServiceImpl implements ImRouterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImRouterServiceImpl.class);

    @DubboReference
    private IRouterHandlerRpc iRouterHandlerRpc;

    @Resource
    private RedisTemplate<String  , String> redisTemplate;

    @Override
    public boolean sendMsg(ImMsgBody imMsgBody) {

        //core-server的ip地址+routerHandlerRpc调用的端口
        String bindAddress = redisTemplate.opsForValue().get(ImCoreServerConstants.IM_BIND_IP_KEY + imMsgBody.getAppId() + ":" + imMsgBody.getUserId());

        if (StringUtils.isEmpty(bindAddress)) {
            LOGGER.info("当前访问的目标用户ID：" + imMsgBody.getUserId() + " 不在线或者系统服务已下线");
            return false;
        }

        /**
         * 绑定用户信息到 Dubbo 链路上下文
         */
        RpcContext.getContext().set("ip" , bindAddress);

        /**
         * 调用 im-core-server 服务，根据 Dubbo Cluster 的 Join 回调和 Invoker 方法来选择调用哪一个 机器服务
         * 根据 Dubbo 上下文传入的 IP 参数
         */
        LOGGER.info("Router 已经成功处理接收方为：{} 的消息，回调到 IM 服务器" , imMsgBody.getUserId());
        iRouterHandlerRpc.sendMsg(imMsgBody);
        return false;
    }
}
