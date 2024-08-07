package ystar.im.router.provider.service;

import ystar.im.Domain.Dto.ImMsgBody;

import java.util.List;

public interface ImRouterService {

    boolean sendMsg(ImMsgBody imMsgBody);

    void batchSendMsg(List<ImMsgBody> imMsgBodies);
}