package ystar.im.core.server.interfaces;

import ystar.im.Domain.Dto.ImMsgBody;

import java.util.List;

/**
 * 专门给Router层的服务进行调用的接口
 */
public interface IRouterHandlerRpc {

    /**
     * 按照用户id进行消息的发送
     */
    void sendMsg(ImMsgBody imMsgBody);

    /**
     * 支持这台 IM 服务器批量接收和处理消息，便于在转发消息的时候以服务器为单位而不是用户为单位
     * @param imMsgBodyList
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}