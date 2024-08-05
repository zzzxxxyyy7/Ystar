package ystar.msg.provider.rabbitmq.handler.Impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.JsonObject;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.AppIdEnum;
import ystar.im.router.interfaces.ImRouterRpc;
import ystar.msg.Constants.ImMsgBizCodeEum;
import ystar.msg.provider.Domain.Dto.MessageDTO;
import ystar.msg.provider.Service.Impl.ISmsServiceImpl;
import ystar.msg.provider.rabbitmq.handler.MessageHandler;

@Service
public class SingleMessageHandlerImpl implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISmsServiceImpl.class);

    @DubboReference
    private ImRouterRpc imRouterRpc;

    @Override
    public void onMsgReceive(ImMsgBody imMsgBody) {
        int bizCode = imMsgBody.getBizCode();

        // 直播间聊天消息
        if (ImMsgBizCodeEum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode() == bizCode) {
            MessageDTO messageDto = JSON.parseObject(imMsgBody.getData() , MessageDTO.class);

            // TODO 直播间业务 后续再做处理

            ImMsgBody respMsg = new ImMsgBody();

            // 发送目标用户
            respMsg.setUserId(messageDto.getObjectId());
            respMsg.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
            respMsg.setBizCode(ImMsgBizCodeEum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode());
            respMsg.setMsgId(imMsgBody.getMsgId());

            // 测试 , 存储Data信息
            JSONObject jsonObject = new JSONObject();
            // 内容
            jsonObject.put("content" , messageDto.getContent());
            // 发送方ID
            jsonObject.put("senderId" , imMsgBody.getUserId());
            respMsg.setData(JSON.toJSONString(jsonObject));

            LOGGER.info("成功处理发送方: {} 的消息 ，投递给接收方：{} "  , imMsgBody.getUserId() , messageDto.getObjectId());

            // 调用 RPC 投放给 Router 服务
            imRouterRpc.sendMsg(respMsg);
        }
    }

}
