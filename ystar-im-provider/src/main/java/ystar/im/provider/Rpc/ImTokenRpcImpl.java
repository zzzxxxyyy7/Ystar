package ystar.im.provider.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import ystar.im.interfaces.ImTokenRpc;
import ystar.im.provider.Service.ImTokenService;

@DubboService
public class ImTokenRpcImpl implements ImTokenRpc {
    
    @Resource
    private ImTokenService imTokenService;

    @Override
    public String createImLoginToken(Long userId, int appId) {
        return imTokenService.createImLoginToken(userId, appId);
    }

    @Override
    public Long getUserIdByToken(String token) {
        return imTokenService.getUserIdByToken(token);
    }
}