package com.ystar.user.api.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.ystar.user.api.Service.ILivingRoomService;
import com.ystar.user.api.Vo.LivingRoomInitVO;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.interfaces.IUserRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import ystar.framework.web.starter.context.YStarRequestContext;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.interfaces.ILivingRoomRpc;

@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    @DubboReference
    private ILivingRoomRpc iLivingRoomRpc;

    @DubboReference
    private IUserRpc iUserRpc;

    /**
     * 开播
     * @param type
     * @return
     */
    @Override
    public boolean startingLiving(Integer type) {

        Long userId = YStarRequestContext.getUserId();
        UserDTO user = iUserRpc.getUserById(userId);

        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();

        livingRoomReqDTO.setType(type);
        // 从线程上下文取出
        livingRoomReqDTO.setAnchorId(userId);
        livingRoomReqDTO.setRoomName("YStar官方直播间");
        // 直播间图片取自用户头像
        livingRoomReqDTO.setCovertImg(user.getAvatar());

        return iLivingRoomRpc.startLivingRoom(livingRoomReqDTO) > 0;
    }

    /**
     * 关播
     * @param roomId
     * @return
     */
    @Override
    public boolean closeLiving(Integer roomId) {
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(roomId);
        livingRoomReqDTO.setAnchorId(YStarRequestContext.getUserId());
        return iLivingRoomRpc.closeLiving(livingRoomReqDTO);
    }

    /**
     * 查询某个用户是否在开播
     * @param userId
     * @param roomId
     * @return
     */
    @Override
    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = iLivingRoomRpc.queryByRoomId(roomId);
        LivingRoomInitVO respVO = BeanUtil.copyProperties(respDTO, LivingRoomInitVO.class);
        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) respVO.setAnchor(false);
        else respVO.setAnchor(respDTO.getAnchorId().equals(userId));
        return respVO;
    }
}
