package ystar.living.provider.service;

import com.baomidou.mybatisplus.extension.service.IService;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.provider.Domain.Po.TLivingRoomPo;

/**
* @author Rhss
* @description 针对表【t_living_room】的数据库操作Service
* @createDate 2024-08-05 15:45:50
*/
public interface TLivingRoomService extends IService<TLivingRoomPo> {

    Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO);

    boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO);

    LivingRoomRespDTO queryByRoomId(Integer roomId);
}
