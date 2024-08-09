package com.ystar.user.api.Service;


import com.ystar.common.VO.OnlinePKReqVO;
import com.ystar.user.api.Vo.LivingRoomInitVO;
import ystar.living.Vo.req.LivingRoomReqVO;
import ystar.living.Vo.resp.LivingRoomPageRespVO;

public interface ILivingRoomService {

    boolean startingLiving(Integer type);

    boolean closeLiving(Integer roomId);

    LivingRoomInitVO anchorConfig(Long userId, Integer roomId);

    LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO);

    /**
     * 当PK直播间连上线准备PK时，调用该请求
     */
    boolean onlinePK(OnlinePKReqVO onlinePKReqVO);
}
