package handler.imclient;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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

import java.util.Scanner;
import java.util.UUID;

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
                    protected void initChannel(Channel channel) {
                        System.out.println("初始化连接建立");
                        channel.pipeline().addLast(new ImMsgEncoder());
                        channel.pipeline().addLast(new ImMsgDecoder());
                        channel.pipeline().addLast(new ClientHandler());
                    }
                });

                // 测试代码段1：发送登录消息包，并持续直播间聊天
                ChannelFuture channelFuture;
                try {
                    channelFuture = bootstrap.connect("localhost", 8085).sync();
                    Channel channel = channelFuture.channel();
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("请输入userId：");
                    Long userId = scanner.nextLong();
                    System.out.print("\n请输入objectId：");
                    Long objectId = scanner.nextLong();
                    String token = imTokenRpc.createImLoginToken(userId, AppIdEnum.YStar_LIVE_BIZ.getCode());
                    // 发送登录消息包
                    ImMsgBody imMsgBody = new ImMsgBody();
                    imMsgBody.setUserId(userId);
                    imMsgBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
                    imMsgBody.setToken(token);
                    channel.writeAndFlush(ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(imMsgBody)));
                    // 心跳包机制
                    sendHeartBeat(userId, channel);
                    // 直播间持续聊天
                    while (true) {
                        System.out.println("请输入聊天内容：");
                        String content = scanner.nextLine();
                        ImMsgBody bizBody = new ImMsgBody();
                        bizBody.setUserId(userId);
                        bizBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
                        bizBody.setBizCode(5555);
                        bizBody.setMsgId(UUID.randomUUID().toString());
                        JSONObject jsonObject = new JSONObject();
                        // 目标用户
                        jsonObject.put("objectId", objectId);
                        // 发送内容
                        jsonObject.put("content", content);
                        bizBody.setData(JSON.toJSONString(jsonObject));
                        ImMsg bizMsg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(bizBody));
                        channel.writeAndFlush(bizMsg);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    private void sendHeartBeat(Long userId, Channel channel) {
        new Thread(() -> {
            while (true) {
                try {
                    //每隔30秒发送心跳包
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ImMsgBody imMsgBody = new ImMsgBody();
                imMsgBody.setUserId(userId);
                imMsgBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
                ImMsg heartBeatMsg = ImMsg.build(ImMsgCodeEnum.IM_HEARTBEAT_MSG.getCode(), JSON.toJSONString(imMsgBody));
                channel.writeAndFlush(heartBeatMsg);
            }
        }).start();
    }
}