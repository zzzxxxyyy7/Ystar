package ystar.im.router.provider.service;

import ystar.im.Domain.Dto.ImMsgBody;

public interface ImRouterService {

    boolean sendMsg(ImMsgBody imMsgBody);
}