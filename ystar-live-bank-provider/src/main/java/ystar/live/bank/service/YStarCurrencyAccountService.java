package ystar.live.bank.service;

import ystar.live.bank.Domain.Po.YStarCurrencyAccountPO;
import ystar.live.bank.dto.YStarCurrencyAccountDTO;

public interface YStarCurrencyAccountService {

    /**
     * 新增账户
     */
    boolean insertOne(Long userId);

    /**
     * 增加虚拟货币
     */
    void incr(Long userId, int num);

    /**
     * 扣减虚拟币
     */
    void decr(Long userId, int num);

    /**
     * 查询账户
     */
    YStarCurrencyAccountDTO getByUserId(Long userId);
}