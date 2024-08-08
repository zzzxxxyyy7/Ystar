package ystar.live.bank.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.live.bank.dto.YStarCurrencyAccountDTO;
import ystar.live.bank.service.YStarCurrencyAccountService;

@DubboService
public class YStarCurrencyAccountRpcImpl implements YStarCurrencyAccountService {
    
    @Resource
    private  YStarCurrencyAccountService yStarCurrencyAccountService;

    @Override
    public boolean insertOne(Long userId) {
        return yStarCurrencyAccountService.insertOne(userId);
    }

    @Override
    public void incr(Long userId, int num) {
        yStarCurrencyAccountService.incr(userId, num);
    }

    @Override
    public void decr(Long userId, int num) {
        yStarCurrencyAccountService.decr(userId, num);
    }

    @Override
    public YStarCurrencyAccountDTO getByUserId(Long userId) {
        return yStarCurrencyAccountService.getByUserId(userId);
    }
}