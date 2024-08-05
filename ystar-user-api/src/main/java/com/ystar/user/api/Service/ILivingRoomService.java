package com.ystar.user.api.Service;


import com.ystar.user.api.Vo.LivingRoomInitVO;
import ystar.living.dto.LivingRoomRespDTO;

public interface ILivingRoomService {

    boolean startingLiving(Integer type);

    boolean closeLiving(Integer roomId);

    LivingRoomInitVO anchorConfig(Long userId, Integer roomId);
}
