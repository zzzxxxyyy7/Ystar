package com.ystar.user.api.Service.Impl;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.ystar.common.Dto.SendGiftMq;
import com.ystar.common.utils.ConvertBeanUtils;
import com.ystar.user.api.Service.IGiftService;
import com.ystar.user.api.Vo.GiftConfigVO;
import com.ystar.user.api.Vo.GiftReqVO;
import com.ystar.user.api.error.YStarApiError;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ystar.framework.web.starter.Error.ApiErrorEnum;
import ystar.framework.web.starter.Error.BizBaseErrorEnum;
import ystar.framework.web.starter.Error.ErrorAssert;
import ystar.framework.web.starter.context.YStarRequestContext;
import ystar.gift.dto.GiftConfigDTO;
import ystar.gift.interfaces.IGiftConfigRpc;
import ystar.im.constant.RabbitMqConstants;
import ystar.live.bank.interfaces.YStarCurrencyAccountRpc;

import java.util.List;
import java.util.UUID;

@Service
public class GiftServiceImpl implements IGiftService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiftServiceImpl.class);

    @DubboReference
    private IGiftConfigRpc giftConfigRpc;

    @DubboReference
    private YStarCurrencyAccountRpc yStarCurrencyAccountRpc;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private Cache<Integer, GiftConfigDTO> giftConfigDTOCache;

    @Override
    public List<GiftConfigVO> listGift() {
        List<GiftConfigDTO> giftConfigDTOList = giftConfigRpc.queryGiftList();
        return ConvertBeanUtils.convertList(giftConfigDTOList, GiftConfigVO.class);
    }

    @Override
    public boolean send(GiftReqVO giftReqVO) {
        int giftId = giftReqVO.getGiftId();

        // 查询本地缓存
        GiftConfigDTO giftConfigDTO = giftConfigDTOCache.get(giftId, id -> giftConfigRpc.getByGiftId(giftId));

        ErrorAssert.isNotNull(giftConfigDTO, ApiErrorEnum.GIFT_CONFIG_ERROR);
        ErrorAssert.isTure(!giftReqVO.getReceiverId().equals(giftReqVO.getSenderUserId()), ApiErrorEnum.NOT_SEND_TO_YOURSELF);

        // 进行异步消费
        SendGiftMq sendGiftMq = new SendGiftMq();
        sendGiftMq.setUserId(YStarRequestContext.getUserId());
        sendGiftMq.setGiftId(giftId);
        sendGiftMq.setRoomId(giftReqVO.getRoomId());
        sendGiftMq.setReceiverId(giftReqVO.getReceiverId());
        sendGiftMq.setPrice(giftConfigDTO.getPrice());
        sendGiftMq.setUrl(giftConfigDTO.getSvgaUrl());
        sendGiftMq.setType(giftReqVO.getType());
        // 设置唯一标识UUID，防止重复消费
        sendGiftMq.setUuid(UUID.randomUUID().toString());

        /**
         * MQ 投递消息到下游微服务
         */
        Message message = new Message(JSON.toJSONBytes(sendGiftMq));
        rabbitTemplate.convertAndSend(RabbitMqConstants.SendGift_EXCHANGE, RabbitMqConstants.SendGift_ROUTINGKEY, message ,(msg -> {
            //发送消息 并设置delayedTime
            msg.getMessageProperties().setDelay(0);
            return msg;
        }));

        return true;
    }
}