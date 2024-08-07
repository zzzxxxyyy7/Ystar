package ystar.msg.provider.rabbitmq.handler.Impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.AppIdEnum;
import ystar.im.router.interfaces.ImRouterRpc;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.interfaces.ILivingRoomRpc;
import ystar.msg.Constants.ImMsgBizCodeEum;
import ystar.msg.provider.Domain.Dto.MessageDTO;
import ystar.msg.provider.Service.Impl.ISmsServiceImpl;
import ystar.msg.provider.rabbitmq.handler.MessageHandler;

import java.util.ArrayList;
import java.util.List;

@Service
public class SingleMessageHandlerImpl implements MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISmsServiceImpl.class);

    @DubboReference
    private ImRouterRpc imRouterRpc;

    @DubboReference
    private ILivingRoomRpc iLivingRoomRpc;

    @Override
    public void onMsgReceive(ImMsgBody imMsgBody) {
        int bizCode = imMsgBody.getBizCode();

        // 直播间聊天消息
        if (ImMsgBizCodeEum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode() == bizCode) {
            // 一个人发送，N 个人接收，根据 roomId 获取对应直播间内所有的 userId，构建新的消息体，发送给 N 个人
            MessageDTO messageDto = JSON.parseObject(imMsgBody.getData() , MessageDTO.class);
            // TODO 直播间业务 后续再做处理
            Integer roomId = messageDto.getRoomId();
            /**
             * 获取直播间所以用户 ID
             */
            LivingRoomReqDTO reqDTO = new LivingRoomReqDTO();
            reqDTO.setRoomId(roomId);
            reqDTO.setAppId(imMsgBody.getAppId());
            List<Long> userIdList = iLivingRoomRpc.queryUserIdsByRoomId(reqDTO);

            List<ImMsgBody> imMsgBodies = new ArrayList<>();

            userIdList.forEach(userId -> {
                // 构建传输消息内容
                ImMsgBody respMsg = new ImMsgBody();
                respMsg.setUserId(userId);
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
                imMsgBodies.add(respMsg);
            });

            LOGGER.info("成功处理发送用户: {} 的消息 ，接收房间号：{}" , imMsgBody.getUserId() , roomId);

            // 调用 RPC 投放给 Router 服务
            imRouterRpc.batchSendMsg(imMsgBodies);
        }
    }

}
