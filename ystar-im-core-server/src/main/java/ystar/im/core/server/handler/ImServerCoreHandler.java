package ystar.im.core.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.handler.Impl.ImHandlerFactoryImpl;

public class ImServerCoreHandler extends SimpleChannelInboundHandler {
    
    private ImHandlerFactory imHandlerFactory = new ImHandlerFactoryImpl();
    
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if(!(msg instanceof ImMsg)) {
            throw new IllegalArgumentException("error msg , msg 参数异常 , 协议不通 , msg is :" + msg);
        }
        ImMsg imMsg = (ImMsg) msg;
        imHandlerFactory.doMsgHandler(channelHandlerContext, imMsg);
        // 登录消息包 - 登录token认证 - channel 和 userId 关联
        // 登出消息包 - 正常断开 im 连接使用
        // 业务消息包 - 传输业务信息
        // 心跳消息包 - 正常检测是否还存活
    }
}