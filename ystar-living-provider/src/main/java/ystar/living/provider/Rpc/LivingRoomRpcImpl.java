package ystar.living.provider.Rpc;

import com.ystar.common.VO.PageWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.living.dto.LivingPkRespDTO;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.interfaces.ILivingRoomRpc;
import ystar.living.provider.service.TLivingRoomService;

import java.util.List;

@DubboService
public class LivingRoomRpcImpl implements ILivingRoomRpc {

    @Resource
    private TLivingRoomService tLivingRoomService;

    @Override
    public List<Long> queryUserIdsByRoomId(LivingRoomReqDTO livingRoomReqDTO) {
        return tLivingRoomService.queryUserIdsByRoomId(livingRoomReqDTO);
    }

    @Override
    public PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDTO livingRoomReqDTO) {
        return tLivingRoomService.list(livingRoomReqDTO);
    }

    /**
     * 开播
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        return tLivingRoomService.startLivingRoom(livingRoomReqDTO);
    }

    /**
     * 关播
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO) {
        return tLivingRoomService.closeLiving(livingRoomReqDTO);
    }

    @Override
    public LivingRoomRespDTO queryByRoomId(Integer roomId) {
        return tLivingRoomService.queryByRoomId(roomId);
    }

    @Override
    public LivingPkRespDTO onlinePK(LivingRoomReqDTO livingRoomReqDTO) {
        return tLivingRoomService.onlinePk(livingRoomReqDTO);
    }

    @Override
    public boolean offlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        return tLivingRoomService.offlinePk(livingRoomReqDTO);
    }

    @Override
    public Long queryOnlinePkUserId(Integer roomId) {
        return tLivingRoomService.queryOnlinePkUserId(roomId);
    }
}
