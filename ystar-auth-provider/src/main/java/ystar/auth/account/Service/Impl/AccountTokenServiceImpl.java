package ystar.auth.account.Service.Impl;

import org.springframework.stereotype.Service;
import ystar.auth.account.Service.IAccountTokenService;
import ystar.auth.account.Utils.JwtUtils;


@Service
public class AccountTokenServiceImpl implements IAccountTokenService {
    
    @Override
    public String createAndSaveLoginToken(Long userId) {
        return JwtUtils.generateToken(userId);
    }

    @Override
    public Long getUserIdByToken(String tokenKey) {
        return JwtUtils.getUserIdFromToken(tokenKey);
    }
}