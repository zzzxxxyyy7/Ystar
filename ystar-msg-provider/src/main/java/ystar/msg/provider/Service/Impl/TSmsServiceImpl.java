package ystar.msg.provider.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystar.msg.Constants.MsgSendResultEnum;
import ystar.msg.Dto.MsgCheckDTO;
import ystar.msg.provider.Config.ThreadPoolManager;
import ystar.msg.provider.Domain.Po.TSms;
import ystar.msg.provider.Domain.Mapper.TSmsMapper;
import ystar.msg.provider.Service.TSmsService;
import ystart.framework.redis.starter.key.MsgCacheKeyBuilder;

import java.util.concurrent.TimeUnit;

/**
* @author Rhss
* @description 针对表【t_sms】的数据库操作Service实现
* @createDate 2024-07-31 17:49:19
*/
@Service
public class TSmsServiceImpl extends ServiceImpl<TSmsMapper, TSms>
    implements TSmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TSmsServiceImpl.class);

    @Resource
    private RedisTemplate<String , Object> redisTemplate;

    @Resource
    private MsgCacheKeyBuilder msgCacheKeyBuilder;

    @Resource
    private TSmsMapper tSmsMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        if (StringUtils.isEmpty(phone)) return MsgSendResultEnum.MSG_PARAM_ERROR;

        // 生成验证码 6位 60s 不能重复发
        int code = RandomUtils.nextInt(100000 , 999999);

        /**
         * 如果没有验证码，才能执行发送，因为 Redis 的 Reactor 网络模型，必须要加锁
         */
        String msgLoginLockInfoKey = msgCacheKeyBuilder.buildMsgLoginLockInfoKey(phone);
        RLock rLock = redissonClient.getLock(msgLoginLockInfoKey);
        try {
            boolean acquireResult = rLock.tryLock(1L, 2L, TimeUnit.SECONDS);
            if (acquireResult) {
                // 近十分钟短信发送次数
                String msgLoginTimesInfoKey = msgCacheKeyBuilder.buildMsgLoginTimesInfoKey(phone);
                Integer SmsTimes = (Integer) redisTemplate.opsForValue().get(msgLoginTimesInfoKey);
                if (SmsTimes != null && SmsTimes > 5) return MsgSendResultEnum.MSG_TIMES_ERROR;

                // 如果十分钟内没有达到上限，但是仍旧存在短信，则不能再发送
                String MsgKey = msgCacheKeyBuilder.buildMsgLoginInfoKey(phone);
                if (Boolean.TRUE.equals(redisTemplate.hasKey(MsgKey))) return MsgSendResultEnum.MSG_REPEAT_ERROR;

                // 设置验证码到缓存
                redisTemplate.opsForValue().set(MsgKey , code , 60 , TimeUnit.SECONDS);
                if (SmsTimes == null) SmsTimes = 1;
                // 设置十分钟内次数 + 1
                redisTemplate.opsForValue().set(msgLoginTimesInfoKey , SmsTimes + 1 ,
                        10 , TimeUnit.MINUTES);
            } else return MsgSendResultEnum.MSG_REPEAT_ERROR; // 重复发送错误
        } catch (Exception e) {
            throw new RuntimeException("发送短信 获取 RLock 分布式锁失败");
        } finally {
            rLock.unlock();
        }

        // 发送验证码
        ThreadPoolManager.commonAsyncPool.execute(() -> {
            boolean sendResult = mockSendSms(phone, code);
            if (sendResult) insertOne(phone , code);
        });

        return MsgSendResultEnum.SEND_SUCCESS;
    }

    // 记录验证码
    /**
     * 模拟发送短信过程，感兴趣的朋友可以尝试对接一些第三方的短信平台
     *
     * @param phone
     * @param code
     */
    private boolean mockSendSms(String phone, Integer code) {
        try {
            LOGGER.info(" ============= 创建短信发送通道中 ============= ,phone is {},code is {}", phone, code);
            Thread.sleep(1000);
            LOGGER.info(" ============= 短信已经发送成功 ============= ");
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        // 参数校验
        if (StringUtils.isEmpty(phone) || code == null || code < 1000000) return new MsgCheckDTO(false, "参数异常");

        // 查询验证码
        String codeCache = msgCacheKeyBuilder.buildMsgLoginInfoKey(phone);
        Integer cacheKey = (Integer) redisTemplate.opsForValue().get(codeCache);
        if (cacheKey == null || !cacheKey.equals(code)) return new MsgCheckDTO(false , "验证码不正确");
        redisTemplate.delete(codeCache);

        return new MsgCheckDTO(true , "登录成功");
    }

    @Override
    public void insertOne(String phone, Integer code) {
        TSms tSms = new TSms();
        tSms.setCode(code);
        tSms.setPhone(phone);
        tSmsMapper.insert(tSms);
    }
}
