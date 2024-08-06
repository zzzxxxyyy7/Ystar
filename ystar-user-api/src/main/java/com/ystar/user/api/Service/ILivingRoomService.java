package com.ystar.user.api.Service;


import com.ystar.user.api.Vo.LivingRoomInitVO;
import ystar.living.Vo.req.LivingRoomReqVO;
import ystar.living.Vo.resp.LivingRoomPageRespVO;
import ystar.living.dto.LivingRoomRespDTO;

import java.util.List;

public interface ILivingRoomService {

    boolean startingLiving(Integer type);

    boolean closeLiving(Integer roomId);

    LivingRoomInitVO anchorConfig(Long userId, Integer roomId);

    LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO);
}
