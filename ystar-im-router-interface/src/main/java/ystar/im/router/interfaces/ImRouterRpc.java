package ystar.im.router.interfaces;


import ystar.im.Domain.Dto.ImMsgBody;

public interface ImRouterRpc {

    /**
     * 按照用户id进行消息的发送
     */
    boolean sendMsg(ImMsgBody imMsgBody);
}