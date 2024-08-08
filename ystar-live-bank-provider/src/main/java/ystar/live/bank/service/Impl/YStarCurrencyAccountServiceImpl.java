package ystar.live.bank.service.Impl;

import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ystar.live.bank.Domain.Mapper.YStarCurrencyAccountMapper;
import ystar.live.bank.Domain.Po.YStarCurrencyAccountPO;
import ystar.live.bank.constants.TradeTypeEnum;
import ystar.live.bank.dto.AccountTradeReqDTO;
import ystar.live.bank.dto.AccountTradeRespDTO;
import ystar.live.bank.dto.YStarCurrencyAccountDTO;
import ystar.live.bank.service.YStarCurrencyAccountService;
import ystar.live.bank.service.YStarCurrencyTradeService;
import ystart.framework.redis.starter.key.BankProviderCacheKeyBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class YStarCurrencyAccountServiceImpl implements YStarCurrencyAccountService {

    @Resource
    private YStarCurrencyAccountMapper YStarCurrencyAccountMapper;

    @Resource
    private BankProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private RedisTemplate<String , Object> redisTemplate;

    @Resource
    private YStarCurrencyTradeService yStarCurrencyTradeService;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,
            4,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000)
    );

    @Override
    public boolean insertOne(Long userId) {
        try {
            YStarCurrencyAccountPO accountPO = new YStarCurrencyAccountPO();
            accountPO.setUserId(userId);
            YStarCurrencyAccountMapper.insert(accountPO);
            return true;
        } catch (Exception e) {
            //有异常但是不抛出，只为了避免重复创建相同userId的账户
        }
        return false;
    }

    @Override
    public void incr(Long userId, int num) {
        YStarCurrencyAccountMapper.incr(userId, num);
    }

    @Override
    public void decr(Long userId, int num) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);

        // 1 基于Redis的余额扣减
        redisTemplate.opsForValue().decrement(cacheKey, num);

        // 2 做DB层的操作（包括余额扣减和流水记录）
        threadPoolExecutor.execute(() -> {
            // 在异步线程池中完成数据库层的扣减和流水记录，带有事务
            // 异步操作：CAP中的AP，没有追求强一致性，保证最终一致性即可（BASE理论）
            consumeDBHandler(userId, num);
        });
    }

    // 发送礼物数据层的处理
    @Transactional(rollbackFor = Exception.class)
    public void consumeDBHandler(Long userId, int num) {
        // 扣减余额(DB层)
        YStarCurrencyAccountMapper.decr(userId, num);
        // 流水记录
        yStarCurrencyTradeService.insertOne(userId, num * -1, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }

    @Override
    public YStarCurrencyAccountDTO getByUserId(Long userId) {
        return ConvertBeanUtils.convert(YStarCurrencyAccountMapper.selectById(userId), YStarCurrencyAccountDTO.class);
    }

    @Override
    public Integer getBalance(Long userId) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        Integer balance = (Integer) redisTemplate.opsForValue().get(cacheKey);

        if (balance != null) {
            if (balance == -1) {
                return null;
            }
            return balance;
        }

        balance = YStarCurrencyAccountMapper.queryBalance(userId);

        if (balance == null) {
            redisTemplate.opsForValue().set(cacheKey, -1, 1L, TimeUnit.MINUTES);
            return null;
        }

        redisTemplate.opsForValue().set(cacheKey, balance, 30L, TimeUnit.MINUTES);
        return balance;
    }

    @Override
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {

        // 1 余额判断并在Redis中扣减余额
        Long userId = accountTradeReqDTO.getUserId();
        int num = accountTradeReqDTO.getNum();

        String lockKey = "ystar-live-bank-provider:balance:lock:" + userId;
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, 1, 1L, TimeUnit.SECONDS);

        // 判断余额和余额扣减操作要保证原子性
        if (Boolean.TRUE.equals(isLock)) {
            try {
                Integer balance = this.getBalance(userId);
                if (balance == null || balance < num) {
                    return AccountTradeRespDTO.buildFail(userId, "账户余额不足", 1);
                }
                // 封装的方法：包括redis余额扣减和 异步DB层处理
                this.decr(userId, num);
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(500, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 等待0.5~1秒后重试
            consumeForSendGift(accountTradeReqDTO);
        }
        return AccountTradeRespDTO.buildSuccess(userId, "扣费成功");
    }
}