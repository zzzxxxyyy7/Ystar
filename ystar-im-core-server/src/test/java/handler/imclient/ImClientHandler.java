package handler.imclient;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.AppIdEnum;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.common.ImMsgDecoder;
import ystar.im.core.server.common.ImMsgEncoder;
import ystar.im.interfaces.ImTokenRpc;

import java.util.HashMap;
import java.util.Map;

@Service
public class ImClientHandler implements InitializingBean {

    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NioEventLoopGroup clientGroup = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(clientGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        System.out.println("初始化连接建立");
                        channel.pipeline().addLast(new ImMsgEncoder());
                        channel.pipeline().addLast(new ImMsgDecoder());
                        channel.pipeline().addLast(new ClientHandler());
                    }
                });

                //测试代码段1：建立连接并保存channel
                Map<Long, Channel> userIdChannelMap = new HashMap<>();
                for (int i = 0; i < 1; i++) {
                    Long userId = 10000L + i;
                    ChannelFuture channelFuture;
                    try {
                        channelFuture = bootstrap.connect("localhost", 8085).sync();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Channel channel = channelFuture.channel();
                    String token = imTokenRpc.createImLoginToken(userId, AppIdEnum.YStar_LIVE_BIZ.getCode());
                    ImMsgBody imMsgBody = new ImMsgBody();
                    imMsgBody.setUserId(userId);
                    imMsgBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
                    imMsgBody.setToken(token);
                    channel.writeAndFlush(ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(imMsgBody)));
                    userIdChannelMap.put(userId, channel);
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                //测试代码段2：持续发送心跳包
                for (int i = 1 ; i <= 4 ; ++i){
                    for (Long userId : userIdChannelMap.keySet()) {
                        ImMsgBody heartBeatBody = new ImMsgBody();
                        heartBeatBody.setUserId(userId);
                        heartBeatBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
                        ImMsg heartBeatMsg;
                        if (i != 4)  heartBeatMsg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(heartBeatBody));
                        else heartBeatMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGOUT_MSG.getCode(), JSON.toJSONString(heartBeatBody));
                        userIdChannelMap.get(userId).writeAndFlush(heartBeatMsg);
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }
}