package ystar.im.core.server.interfaces;

import ystar.im.Domain.Dto.ImMsgBody;

/**
 * 专门给Router层的服务进行调用的接口
 */
public interface IRouterHandlerRpc {

    /**
     * 按照用户id进行消息的发送
     */
    void sendMsg(Long objectId , String msgJson);
}