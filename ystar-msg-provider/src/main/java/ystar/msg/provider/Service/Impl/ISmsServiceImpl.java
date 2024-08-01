package ystar.msg.provider.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
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
import ystar.msg.provider.Domain.Po.SmsPo;
import ystar.msg.provider.Domain.Mapper.ISmsMapper;
import ystar.msg.provider.Service.ISmsService;
import ystart.framework.redis.starter.key.MsgCacheKeyBuilder;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
* @author Rhss
* @description 针对表【t_sms】的数据库操作Service实现
* @createDate 2024-07-31 17:49:19
*/
@Service
public class ISmsServiceImpl extends ServiceImpl<ISmsMapper, SmsPo>
    implements ISmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ISmsServiceImpl.class);

    @Resource
    private RedisTemplate<String , Object> redisTemplate;

    @Resource
    private MsgCacheKeyBuilder msgCacheKeyBuilder;

    @Resource
    private ISmsMapper iSmsMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        // 生成验证码 4位 60s 不能重复发
        int code = RandomUtils.nextInt(1000 , 10000);

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
            boolean sendResult = sendSmsToCCP(phone, code);
            if (sendResult) insertOne(phone , code);
        });

        return MsgSendResultEnum.SEND_SUCCESS;
    }

    /**
     * 通过容联云平台发送短信，可以将账号配置信息抽取到Nacos配置中心
     * @param phone
     * @param code
     * @return
     */
    public boolean sendSmsToCCP(String phone, Integer code) {
        try {
            CCPRestSmsSDK sdk = getCcpRestSmsSDK();
            // 测试开发短信模板：【云通讯】您的验证码是{1}，请于{2}分钟内正确输入。其中{1}和{2}为短信模板参数
            // 示例：你的验证码是 xxxxxx ，请在 x 分钟内登录
            // 验证码只能是 1 - 4 位数
            String[] datas = {String.valueOf(code), "1"};
            HashMap<String, Object> result = sdk.sendTemplateSMS(phone, "1", datas);
            if ("000000".equals(result.get("statusCode"))) {
                // 正常返回输出data包体信息（map）
                HashMap<String, Object> data = (HashMap<String, Object>) result.get("data");
                Set<String> keySet = data.keySet();
                for (String key : keySet) {
                    Object object = data.get(key);
                    LOGGER.info(key + " = " + object);
                }
            } else {
                // 异常返回输出错误码和错误信息
                LOGGER.error("错误码=" + result.get("statusCode") + " 错误信息= " + result.get("statusMsg"));
            }
            return true;
        }catch (Exception e) {
            LOGGER.error("[sendSmsToCCP] error is ", e);
            throw new RuntimeException(e);
        }
    }

    private static CCPRestSmsSDK getCcpRestSmsSDK() {
        // 生产环境请求地址：app.cloopen.com
        String serverIp = "app.cloopen.com";
        // 请求端口
        String serverPort = "8883";
        // 主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId = "2c94811c9035ff9f01910ae3dacb2c0e";
        String accountToken = "6de713293e2e4ea5879400f4e39c8543";
        // 请使用管理控制台中已创建应用的APPID
        String appId = "2c94811c9035ff9f01910ae3dc452c15";
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(serverIp, serverPort);
        sdk.setAccount(accountSId, accountToken);
        sdk.setAppId(appId);
        sdk.setBodyType(BodyType.Type_JSON);
        return sdk;
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        // 查询验证码
        String codeCache = msgCacheKeyBuilder.buildMsgLoginInfoKey(phone);
        Integer cacheKey = (Integer) redisTemplate.opsForValue().get(codeCache);
        if (cacheKey == null || !cacheKey.equals(code)) return new MsgCheckDTO(false , "验证码不正确");
        redisTemplate.delete(codeCache);

        return new MsgCheckDTO(true , "登录成功");
    }

    @Override
    public void insertOne(String phone, Integer code) {
        SmsPo smsPo = new SmsPo();
        smsPo.setCode(code);
        smsPo.setPhone(phone);
        iSmsMapper.insert(smsPo);
    }
}
