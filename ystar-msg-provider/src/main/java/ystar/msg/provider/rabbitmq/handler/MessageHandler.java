package ystar.msg.provider.rabbitmq.handler;

import ystar.im.Domain.Dto.ImMsgBody;

public interface MessageHandler {
    /**
     * 处理im发送过来的业务消息包
     */
    void onMsgReceive(ImMsgBody imMsgBody);
}