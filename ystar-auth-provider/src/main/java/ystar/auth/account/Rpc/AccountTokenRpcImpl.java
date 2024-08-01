package ystar.auth.account.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.auth.account.Service.IAccountTokenService;
import ystar.auth.account.interfaces.IAccountTokenRPC;

@DubboService
public class AccountTokenRpcImpl implements IAccountTokenRPC {
    
    @Resource
    private IAccountTokenService accountTokenService;

    @Override
    public String createAndSaveLoginToken(Long userId) {
        return accountTokenService.createAndSaveLoginToken(userId);
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        return accountTokenService.getUserIdByToken(tokenKey);
    }
}