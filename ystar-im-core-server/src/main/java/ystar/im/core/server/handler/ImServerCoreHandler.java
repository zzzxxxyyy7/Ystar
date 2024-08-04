package ystar.im.core.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import ystar.im.core.server.common.ChannelHandlerContextCache;
import ystar.im.core.server.common.ImContextAttr;
import ystar.im.core.server.common.ImContextUtils;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.constants.ImCoreServerConstants;
import ystar.im.core.server.handler.Impl.LogoutMsgHandler;

@Component
@ChannelHandler.Sharable
public class ImServerCoreHandler extends SimpleChannelInboundHandler {

    @Resource
    private ImHandlerFactory imHandlerFactory;

    @Resource
    private LogoutMsgHandler logoutMsgHandler;

    @Resource
    private RedisTemplate<String , String> redisTemplate;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if(!(msg instanceof ImMsg)) {
            throw new IllegalArgumentException("error msg, msg is :" + msg);
        }
        ImMsg imMsg = (ImMsg) msg;
        imHandlerFactory.doMsgHandler(channelHandlerContext, imMsg);
    }

    /**
     * 客户端正常或意外掉线，都会触发这里，用来解决客户端意外断线的用户下线问题
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId != null && appId != null) {
            logoutMsgHandler.handlerLogout(userId, appId);
        }
    }
}