package ystar.living.provider.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ystar.common.VO.PageWrapper;
import ystar.im.core.server.dto.ImOfflineDto;
import ystar.im.core.server.dto.ImOnlineDto;
import ystar.living.dto.LivingPkRespDTO;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.provider.Domain.Po.TLivingRoomPo;

import java.util.List;

/**
* @author Rhss
* @description 针对表【t_living_room】的数据库操作Service
* @createDate 2024-08-05 15:45:50
*/
public interface TLivingRoomService extends IService<TLivingRoomPo> {

    /**
     * 用户上线
     * @param imOnlineDto
     */
    void userOnlineHandler(ImOnlineDto imOnlineDto);

    /**
     * 用户下线
     * @param imOfflineDto
     */
    void userOfflineHandler(ImOfflineDto imOfflineDto);

    Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO);

    boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO);

    LivingRoomRespDTO queryByRoomId(Integer roomId);

    /**
     * 直播间列表的分页查询
     *
     * @param livingRoomReqDTO
     * @return
     */
    PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDTO livingRoomReqDTO);

    /**
     * 定时任务调度刷新缓存和数据库直播间集合差异
     * @param type
     * @return
     */
    List<LivingRoomRespDTO> listAllLivingRoomFromDB(Integer type);

    List<Long> queryUserIdsByRoomId(LivingRoomReqDTO livingRoomReqDTO);

    /**
     * 用户在pk直播间中，连上线请求
     */
    LivingPkRespDTO onlinePk(LivingRoomReqDTO livingRoomReqDTO);

    /**
     * 用户在pk直播间下线
     */
    boolean offlinePk(LivingRoomReqDTO livingRoomReqDTO);

    /**
     * 根据roomId查询当前pk人是谁
     *
     * @param roomId
     * @return
     */
    Long queryOnlinePkUserId(Integer roomId);
}
