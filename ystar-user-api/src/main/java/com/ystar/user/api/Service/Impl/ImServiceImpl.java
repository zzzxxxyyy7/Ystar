package com.ystar.user.api.Service.Impl;

import com.ystar.user.api.Service.ImService;
import com.ystar.user.api.Vo.ImConfigVO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import ystar.framework.web.starter.context.YStarRequestContext;
import ystar.im.constant.AppIdEnum;
import ystar.im.interfaces.ImTokenRpc;

import java.util.Collections;
import java.util.List;

/**
 * 客户端通过这个服务获取到 IM 服务的地址和配置信息
 */
@Service
public class ImServiceImpl implements ImService {

    /**
     * 获取连接 IM 服务所必须的 Token
     */
    @DubboReference
    private ImTokenRpc imTokenRpc;

    /**
     * 服务发现所需类，Nacos 提供的服务发现组件
     */
    @Resource
    private DiscoveryClient discoveryClient;

    // 这里是通过DiscoveryClient获取Nacos中的注册信息，我们还可以通过在ImCoreServer中写一个rpc方法，和构建Router功能是一样，获取我们在启动参数中的添加的DUBBO注册ip
    @Override
    public ImConfigVO getImConfig() {

        ImConfigVO imConfigVO = new ImConfigVO();
        // TODO 暂时写死只有直播业务
        imConfigVO.setToken(imTokenRpc.createImLoginToken(YStarRequestContext.getUserId(), AppIdEnum.YStar_LIVE_BIZ.getCode()));

        // 获取到在Nacos中注册的对应服务名的实例集合
        List<ServiceInstance> serverInstanceList = discoveryClient.getInstances("ystar-im-core-server");

        // 打乱集合顺序
        Collections.shuffle(serverInstanceList);

        // 随机获取其中一个
        ServiceInstance serviceInstance = serverInstanceList.get(0);

        // 同时记录 TCP 和 WS 服务暴露地址
        imConfigVO.setTcpImServerAddress(serviceInstance.getHost() + ":8085");
        imConfigVO.setWsImServerAddress(serviceInstance.getHost() + ":8086");
        return imConfigVO;
    }
}