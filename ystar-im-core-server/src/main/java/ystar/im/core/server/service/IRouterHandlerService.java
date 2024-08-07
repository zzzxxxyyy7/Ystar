package ystar.im.core.server.service;


import ystar.im.Domain.Dto.ImMsgBody;

import java.util.List;

public interface IRouterHandlerService {

    /**
     * 收到业务消息时，进行处理
     * @param imMsgBody
     */
    void onReceive(ImMsgBody imMsgBody);

    /**
     *
     * @param imMsgBody
     */
    boolean sendMsgToClient(ImMsgBody imMsgBody);

    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}
