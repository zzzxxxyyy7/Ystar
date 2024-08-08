package ystar.live.bank.rabbitmq.consumer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.Channel;
import com.ystar.common.Dto.SendGiftMq;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.AppIdEnum;
import ystar.im.constant.RabbitMqConstants;
import ystar.im.router.Constants.ImMsgBizCodeEnum;
import ystar.im.router.interfaces.ImRouterRpc;
import ystar.live.bank.dto.AccountTradeReqDTO;
import ystar.live.bank.dto.AccountTradeRespDTO;
import ystar.live.bank.interfaces.YStarCurrencyAccountRpc;
import ystart.framework.redis.starter.key.GiftProviderCacheKeyBuilder;

import java.util.concurrent.TimeUnit;

@Component
public class SendGiftConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendGiftConsumer.class);

    @DubboReference
    private YStarCurrencyAccountRpc yStarCurrencyAccountRpc;

    @DubboReference
    private ImRouterRpc routerRpc;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @RabbitListener(queues = RabbitMqConstants.SendGift_QUEUE)
    public void consumeSendGift(Message message, Channel channel){
        SendGiftMq sendGiftMq = JSON.parseObject(message.getBody(), SendGiftMq.class);

        // 判断这条消息有没有在缓存中
        String mqConsumerKey = cacheKeyBuilder.buildGiftConsumeKey(sendGiftMq.getUuid());
        Boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(mqConsumerKey, -1, 5L, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(lockStatus)) {
            // 代表曾经消费过，防止重复消费
            return;
        }

        Long userId = sendGiftMq.getUserId();
        AccountTradeReqDTO accountTradeReqDTO = new AccountTradeReqDTO();
        accountTradeReqDTO.setUserId(userId);
        accountTradeReqDTO.setNum(sendGiftMq.getPrice());
        AccountTradeRespDTO tradeRespDTO = yStarCurrencyAccountRpc.consumeForSendGift(accountTradeReqDTO);

        // 如果余额扣减成功
        ImMsgBody imMsgBody = new ImMsgBody();
        imMsgBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
        JSONObject jsonObject = new JSONObject();

        if (tradeRespDTO.isSuccess()) {
            // TODO 触发礼物特效推送功能
            imMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_SUCCESS.getCode());
            imMsgBody.setUserId(sendGiftMq.getReceiverId());// 传达给接收者
            jsonObject.put("url", sendGiftMq.getUrl());
        } else {
            // TODO 利用IM将发送失败的消息告知用户
            imMsgBody.setBizCode(ImMsgBizCodeEnum.LIVING_ROOM_SEND_GIFT_FAIL.getCode());
            imMsgBody.setUserId(userId);// 失败信息只传达给发送者
            jsonObject.put("msg", tradeRespDTO.getMsg());
            LOGGER.info("[sendGiftConsumer] send fail, msg is {}", tradeRespDTO.getMsg());
        }

        imMsgBody.setData(jsonObject.toJSONString());
        routerRpc.sendMsg(imMsgBody);
    }
}