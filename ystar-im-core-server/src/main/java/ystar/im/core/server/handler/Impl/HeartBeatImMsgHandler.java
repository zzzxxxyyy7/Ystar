package ystar.im.core.server.handler.Impl;

import io.netty.channel.ChannelHandlerContext;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.handler.SimpleHandler;

/**
 * 心跳消息处理器
 */
public class HeartBeatImMsgHandler implements SimpleHandler {
    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        System.out.println("[heartbear]:" + imMsg);
        ctx.writeAndFlush(imMsg);
    }
}