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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Router 层进行调用 IM 服务回传信息
 */
@Service
public class ImRouterServiceImpl implements ImRouterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImRouterServiceImpl.class);

    @DubboReference
    private IRouterHandlerRpc iRouterHandlerRpc;

    @Resource
    private RedisTemplate<String  , String> redisTemplate;

    /**
     * 发送消息给指定用户
     * @param imMsgBody
     * @return
     */
    @Override
    public boolean sendMsg(ImMsgBody imMsgBody) {

        //core-server的ip地址+routerHandlerRpc调用的端口
        String bindAddress = redisTemplate.opsForValue().get(ImCoreServerConstants.IM_BIND_IP_KEY + imMsgBody.getAppId() + ":" + imMsgBody.getUserId());

        if (StringUtils.isEmpty(bindAddress)) {
            LOGGER.info("当前访问的目标用户ID：" + imMsgBody.getUserId() + " 不在线或者系统服务已下线");
            return false;
        }

        bindAddress = bindAddress.substring(bindAddress.indexOf("%"));

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

    /**
     * 批量发送消息给直播间所有用户，根据不同的 IM 服务器来区分，以服务器为单位发送数据
     * @param imMsgBodies
     */
    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodies) {
        // 拿到直播间内所有 userId
        List<Long> userIdList = imMsgBodies.stream().map(ImMsgBody::getUserId).toList();
        // 封装 userId 对应消息体的 Map 集合
        Map<Long, ImMsgBody> userIdMsgMap = imMsgBodies.stream().collect(Collectors.toMap(ImMsgBody::getUserId, x -> x));
        // 保证整个集合内 APPID 均是统一
        int appId = imMsgBodies.get(0).getAppId();
        List<String> redisKey = new ArrayList<>();

        userIdList.forEach(userId -> {
            String cacheKey = ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId;
            redisKey.add(cacheKey);
        });

        /**
         * 拿到所有 userId 绑定的 IM 服务器地址
         */
        List<String> ipList = redisTemplate.opsForValue().multiGet(redisKey);
        Map<String , List<Long>> userMap = new HashMap<>();

        ipList.forEach(ip -> {
            String currentIp = ip.substring(ip.indexOf("%"));
            Long userId = Long.valueOf(ip.substring(ip.indexOf("%" , -1)));
            List<Long> userIdLists = userMap.get(currentIp);
            if (userIdLists == null) userIdLists = new ArrayList<>();
            userIdLists.add(userId);
            userMap.put(ip , userIdLists);
        });

        /**
         * 将同一台 IM 服务器对应的数据封装到同一个 List 集合中
         */
        for (String currentIp : userMap.keySet()) {
            List<ImMsgBody> batchSendMsgGroupByIpList = new ArrayList<>();
            RpcContext.getContext().set("ip" , currentIp);
            List<Long> ipBindUserIds = userMap.get(currentIp);
            for (Long ipBindUserId : ipBindUserIds) {
                ImMsgBody imMsgBody = userIdMsgMap.get(ipBindUserId);
                batchSendMsgGroupByIpList.add(imMsgBody);
            }
            iRouterHandlerRpc.batchSendMsg(batchSendMsgGroupByIpList);
        }

    }
}
