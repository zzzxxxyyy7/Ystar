package ystar.im.core.server.handler.WebSocket;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.impl.Environment;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.handler.Impl.LoginMsgHandler;
import ystar.im.interfaces.ImTokenRpc;

/**
 * WebSocket协议的握手连接处理器
 */
@Component
@ChannelHandler.Sharable
@RefreshScope
public class WsSharkHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsSharkHandler.class);

    @Value("${ystar.im.ws.port}")
    private int port;
    
    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Resource
    private LoginMsgHandler loginMsgHandler;

    private WebSocketServerHandshaker webSocketServerHandshaker;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 握手连接接入ws
        if (msg instanceof FullHttpRequest) {
            handlerHttpRequest(ctx, (FullHttpRequest) msg);
            return;
        }
        // 正常关闭链路
        if (msg instanceof CloseWebSocketFrame) {
            webSocketServerHandshaker.close(ctx.channel(), (CloseWebSocketFrame) ((WebSocketFrame) msg).retain());
            return;
        }
        ctx.fireChannelRead(msg);
    }

    private void handlerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
        String serverIp = "139.224.188.112";
        String webSocketUrl = "ws://" + serverIp + ":" + port;
        // 构造握手响应返回
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketUrl, null, false);
        // 进行参数校验
        String uri = msg.uri();
        String[] paramArr = uri.split("/");
        String token = paramArr[1];
        Long userId = Long.valueOf(paramArr[2]);
        Long queryUserId = imTokenRpc.getUserIdByToken(token);

        //token的最后与%拼接的就是appId
        Integer appId = Integer.valueOf(token.substring(token.indexOf("%") + 1));
        if (queryUserId == null || !queryUserId.equals(userId)) {
            LOGGER.error("[WsSharkHandler] token 校验不通过！");
            ctx.close();
            return;
        }

        // 参数校验通过，建立ws握手连接
        webSocketServerHandshaker = wsFactory.newHandshaker(msg);
        if (webSocketServerHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }

        ChannelFuture channelFuture = webSocketServerHandshaker.handshake(ctx.channel(), msg);
        // 首次握手建立ws连接后，返回一定的内容给到客户端
        if (channelFuture.isSuccess()) {
            Integer code = Integer.valueOf(paramArr[3]);
            Integer roomId = null;
            if (code == ParamCodeEnum.LIVING_ROOM_LOGIN.getCode()) {
                roomId = Integer.valueOf(paramArr[4]);
            }
            //这里调用login消息包的处理器，直接做login成功的处理
            loginMsgHandler.loginSuccessHandler(ctx, userId, appId, roomId);
            LOGGER.info("[WsSharkHandler] channel is connect");
        }
    }

    enum ParamCodeEnum {
        LIVING_ROOM_LOGIN(1001, "直播间登录");
        int code;
        String desc;
        ParamCodeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode() {
            return code;
        }
        public String getDesc() {
            return desc;
        }
    }
}