package ystar.living.provider.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.common.utils.CommonStatusEnum;
import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.provider.Domain.Mapper.TLivingRoomMapper;
import ystar.living.provider.Domain.Mapper.TLivingRoomRecordMapper;
import ystar.living.provider.Domain.Po.TLivingRoomPo;
import ystar.living.provider.Domain.Po.TLivingRoomRecordPo;
import ystar.living.provider.service.TLivingRoomService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author Rhss
* @description 针对表【t_living_room】的数据库操作Service实现
* @createDate 2024-08-05 15:45:50
*/
@Service
public class TLivingRoomServiceImpl extends ServiceImpl<TLivingRoomMapper, TLivingRoomPo>
    implements TLivingRoomService{

    @Resource
    private TLivingRoomMapper tLivingRoomMapper;

    @Resource
    private TLivingRoomRecordMapper tLivingRoomRecordMapper;

    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        TLivingRoomPo tLivingRoomPo = ConvertBeanUtils.convert(livingRoomReqDTO , TLivingRoomPo.class);
        // 状态合法
        tLivingRoomPo.setStatus(CommonStatusEnum.VALID_STATUS.getCode());
        tLivingRoomPo.setStart_time(new Date());
        tLivingRoomMapper.insert(tLivingRoomPo);
        // 直播间 ID
        return tLivingRoomPo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO) {
        TLivingRoomPo tLivingRoomPo = tLivingRoomMapper.selectById(livingRoomReqDTO.getRoomId());

        // 当前直播间不存在
        if (tLivingRoomPo == null) return false;

        // 没有权限关闭这个直播间
        if (!(tLivingRoomPo.getAnchor_id().equals(livingRoomReqDTO.getAnchorId()))) return false;

        TLivingRoomRecordPo tLivingRoomRecordPo = ConvertBeanUtils.convert(tLivingRoomPo , TLivingRoomRecordPo.class);
        tLivingRoomRecordPo.setEnd_time(new Date());
        tLivingRoomRecordPo.setStatus(CommonStatusEnum.VALID_STATUS.getCode());

        tLivingRoomMapper.deleteById(livingRoomReqDTO.getRoomId());
        tLivingRoomRecordMapper.insert(tLivingRoomRecordPo);
        return false;
    }

    /**
     * 判断当前直播间是否在线、有效
     * @param roomId
     * @return
     */
    @Override
    public LivingRoomRespDTO queryByRoomId(Integer roomId) {
        LambdaQueryWrapper<TLivingRoomPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TLivingRoomPo::getId, roomId);
        queryWrapper.eq(TLivingRoomPo::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        TLivingRoomPo livingRoomPO = tLivingRoomMapper.selectOne(queryWrapper);
        return BeanUtil.copyProperties(livingRoomPO, LivingRoomRespDTO.class);
    }
}




