package ystar.live.bank.interfaces;

import ystar.live.bank.dto.YStarCurrencyAccountDTO;

public interface YStarCurrencyAccountRpc {

    /**
     * 新增账户
     */
    boolean insertOne(Long userId);

    /**
     * 增加Star币
     */
    void incr(Long userId, int num);

    /**
     * 扣减Star币
     */
    void decr(Long userId, int num);

    /**
     * 查询账户
     */
    YStarCurrencyAccountDTO getByUserId(Long userId);
}