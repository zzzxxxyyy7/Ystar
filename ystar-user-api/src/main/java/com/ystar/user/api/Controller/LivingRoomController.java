package com.ystar.user.api.Controller;

import com.ystar.common.VO.WebResponseVO;
import com.ystar.user.api.Service.ILivingRoomService;
import ystar.living.Vo.req.LivingRoomReqVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ystar.framework.web.starter.context.YStarRequestContext;

@RestController
@RequestMapping("/living")
public class LivingRoomController {

    @Resource
    private ILivingRoomService iLivingRoomService;

    @PostMapping("/list")
    public WebResponseVO list(LivingRoomReqVO livingRoomReqVO) {
        if (livingRoomReqVO == null || livingRoomReqVO.getType() == null) return WebResponseVO.errorParam("需要给定直播间类型");
        if (livingRoomReqVO.getPage() <= 0 || livingRoomReqVO.getPageSize() > 100) return WebResponseVO.errorParam("分页查询参数错误");
        return WebResponseVO.success(iLivingRoomService.list(livingRoomReqVO));
    }

    @PostMapping("startingLiving")
    public WebResponseVO startingLiving(Integer type) {
        // 调用 RPC 往数据库写入一条记录即可
        if (type == null) return WebResponseVO.errorParam("需要给定直播间类型");

        boolean result = iLivingRoomService.startingLiving(type);

        if (result) return WebResponseVO.success();

        return WebResponseVO.bizError("当前开播异常");
    }

    @PostMapping("/closeLiving")
    public WebResponseVO closeLiving(Integer roomId) {
        if (roomId == null) return WebResponseVO.errorParam("需要给定直播间id");
        boolean status = iLivingRoomService.closeLiving(roomId);
        if (status) return WebResponseVO.success();
        return WebResponseVO.bizError("关播异常，请稍后再试");
    }

    @PostMapping("/anchorConfig")
    public WebResponseVO anchorConfig(Integer roomId) {
        Long userId = YStarRequestContext.getUserId();
        return WebResponseVO.success(iLivingRoomService.anchorConfig(userId, roomId));
    }
}
