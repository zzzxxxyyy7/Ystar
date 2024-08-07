package com.ystar.user.api.Controller;

import com.ystar.common.VO.WebResponseVO;
import com.ystar.user.api.Service.ILivingRoomService;
import com.ystar.user.api.error.YStarApiError;
import ystar.framework.web.starter.Config.RequestLimit;
import ystar.framework.web.starter.Error.BizBaseErrorEnum;
import ystar.framework.web.starter.Error.ErrorAssert;
import ystar.living.Vo.req.LivingRoomReqVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ystar.framework.web.starter.context.YStarRequestContext;
import ystar.living.Vo.resp.LivingRoomPageRespVO;

@RestController
@RequestMapping("/living")
public class LivingRoomController {

    @Resource
    private ILivingRoomService iLivingRoomService;

    @PostMapping("/list")
    public WebResponseVO list(LivingRoomReqVO livingRoomReqVO) {
        ErrorAssert.isTure(livingRoomReqVO != null || livingRoomReqVO.getType() != null, YStarApiError.LIVING_ROOM_TYPE_MISSING);
        ErrorAssert.isTure(livingRoomReqVO.getPage() > 0 || livingRoomReqVO.getPageSize() <= 100, BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(iLivingRoomService.list(livingRoomReqVO));
    }

    @RequestLimit(limit = 1, second = 10, msg = "开播请求过于频繁，请稍后再试")
    @PostMapping("startingLiving")
    public WebResponseVO startingLiving(Integer type) {
        // 调用 RPC 往数据库写入一条记录即可
        if (type == null) return WebResponseVO.errorParam("需要给定直播间类型");

        boolean result = iLivingRoomService.startingLiving(type);

        if (result) return WebResponseVO.success();

        return WebResponseVO.bizError("当前开播异常");
    }

    @RequestLimit(limit = 1, second = 10, msg = "关播请求过于频繁，请稍后再试")
    @PostMapping("/closeLiving")
    public WebResponseVO closeLiving(Integer roomId) {
        ErrorAssert.isNotNull(roomId, BizBaseErrorEnum.PARAM_ERROR);
        boolean status = iLivingRoomService.closeLiving(roomId);
        if (status) {
            return WebResponseVO.success();
        }
        return WebResponseVO.bizError("关播异常，请稍后再试");
    }

    @PostMapping("/anchorConfig")
    public WebResponseVO anchorConfig(Integer roomId) {
        Long userId = YStarRequestContext.getUserId();
        return WebResponseVO.success(iLivingRoomService.anchorConfig(userId, roomId));
    }
}
