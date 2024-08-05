package handler.imclient;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.core.server.common.ImMsg;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ImMsg imMsg = (ImMsg) msg;
        ImMsgBody respAckMsg = JSON.parseObject(new String(imMsg.getBody()), ImMsgBody.class);
        if (imMsg.getCode() == ImMsgCodeEnum.IM_BIZ_MSG.getCode()) {
            //是业务消息，就要发回ACK
            ImMsgBody ackBody = new ImMsgBody();
            ackBody.setUserId(respAckMsg.getUserId());
            ackBody.setAppId(respAckMsg.getAppId());
            ackBody.setMsgId(respAckMsg.getMsgId());
            ImMsg ackMsg = ImMsg.build(ImMsgCodeEnum.IM_ACK_MSG.getCode(), JSON.toJSONString(ackBody));
            ctx.writeAndFlush(ackMsg);
        }
        // ClientHandler中
        System.out.println("【服务端响应" + respAckMsg.getData()  + "】 result is " + respAckMsg);
    }
}