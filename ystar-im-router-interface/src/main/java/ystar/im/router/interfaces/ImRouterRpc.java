package ystar.im.router.interfaces;


import ystar.im.Domain.Dto.ImMsgBody;

import java.util.List;

public interface ImRouterRpc {

    /**
     * 按照用户id进行消息的发送
     */
    boolean sendMsg(ImMsgBody imMsgBody);

    /**
     * Router 服务转发数据，支持批量发送消息，在直播间内
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodies);
}