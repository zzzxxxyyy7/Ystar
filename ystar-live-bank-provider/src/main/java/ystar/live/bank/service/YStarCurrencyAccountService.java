package ystar.live.bank.service;

import ystar.live.bank.dto.AccountTradeReqDTO;
import ystar.live.bank.dto.AccountTradeRespDTO;
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
     * @param userId
     * @return
     */
    YStarCurrencyAccountDTO getByUserId(Long userId);

    /**
     * 查询账户余额
     */
    Integer getBalance(Long userId);

    /**
     * 专门给送礼用的扣减库存逻辑，进行了高并发优化
     */
    AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO);


}