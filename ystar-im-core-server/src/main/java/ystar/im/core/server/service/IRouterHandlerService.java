package ystar.im.core.server.service;


import ystar.im.Domain.Dto.ImMsgBody;

public interface IRouterHandlerService {

    void onReceive(ImMsgBody imMsgBody);

}
