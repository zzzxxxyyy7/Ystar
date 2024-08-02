package ystar.im.core.server.handler.Impl;

import io.netty.channel.ChannelHandlerContext;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.handler.SimpleHandler;

/**
 * 登出消息处理器
 */
public class LogoutMsgHandler implements SimpleHandler {
    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        System.out.println("[logout]:" + imMsg);
        ctx.writeAndFlush(imMsg);
    }
}