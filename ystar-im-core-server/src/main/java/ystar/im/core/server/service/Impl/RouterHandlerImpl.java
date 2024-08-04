package ystar.im.core.server.service.Impl;

import com.alibaba.fastjson2.JSON;
import io.micrometer.common.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.ImMsgCodeEnum;
import ystar.im.core.server.common.ChannelHandlerContextCache;
import ystar.im.core.server.common.ImMsg;
import ystar.im.core.server.service.IRouterHandlerService;

@Service
public class RouterHandlerImpl implements IRouterHandlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterHandlerImpl.class);

    @Override
    public void onReceive(ImMsgBody imMsgBody) {
        // 接收方 ID
        Long userId = imMsgBody.getUserId();
        LOGGER.info("IM 服务收到来自 Router 的回调消息，接收方是 userId： {}" , imMsgBody.getUserId());

        /**
         * 根据 UserId 拿到绑定的信道 Channel
         */
        ChannelHandlerContext ctx = ChannelHandlerContextCache.get(userId);

        /**
         * 如果 Router 判断的时候用户还在线，但是消息抵达 IM 服务的时候用户已经下线，还需要第二步判断
         */
        if (ctx != null) {
            ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(imMsgBody));
            ctx.writeAndFlush(respMsg);
        }
    }
}
