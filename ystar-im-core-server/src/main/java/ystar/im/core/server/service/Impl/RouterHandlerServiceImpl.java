package ystar.im.core.server.service.Impl;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.core.server.common.ChannelHandlerContextCache;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.service.IMsgAckCheckService;
import ystar.im.core.server.service.IRouterHandlerService;

import java.util.UUID;

@Service
public class RouterHandlerServiceImpl implements IRouterHandlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterHandlerServiceImpl.class);

    @Resource
    private IMsgAckCheckService iMsgAckCheckService;

    @Override
    public void onReceive(ImMsgBody imMsgBody) {
        sendMsgToClient(imMsgBody);
    }

    @Override
    public boolean sendMsgToClient(ImMsgBody imMsgBody) {
        // 接收方 ID
        Long userId = imMsgBody.getUserId();

        /**
         * 根据 UserId 拿到绑定的信道 Channel
         */
        ChannelHandlerContext ctx = ChannelHandlerContextCache.get(userId);

        if (ctx != null) {
            ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(imMsgBody));

            LOGGER.info("已经成功收到回调消息 ， 并且写回 channel");

            ctx.writeAndFlush(respMsg);

            return true;
        }
        return false;
    }
}
