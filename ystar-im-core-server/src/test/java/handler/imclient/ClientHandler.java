package handler.imclient;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.core.server.common.ImMsg;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ImMsg imMsg = (ImMsg) msg;
        ImMsgBody imMsgBody = JSON.parseObject(new String(imMsg.getBody()), ImMsgBody.class);
        // ClientHandler中
        System.out.println("【服务端响应" + imMsgBody.getData()  + "】 result is " + imMsgBody);
    }
}