package ystar.im.core.server.handler.Impl;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.handler.SimpleHandler;

/**
 * 业务消息处理器
 */
@Component
public class BizImMsgHandler implements SimpleHandler {
    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        System.out.println("[bizImMsg]:" + imMsg);
        ctx.writeAndFlush(imMsg);
    }
}