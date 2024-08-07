package com.ystar.user.api.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.ystar.common.VO.PageWrapper;
import com.ystar.common.utils.CommonStatusEnum;
import com.ystar.common.utils.ConvertBeanUtils;
import com.ystar.user.api.Service.ILivingRoomService;
import com.ystar.user.api.Vo.LivingRoomInitVO;
import ystar.living.Vo.req.LivingRoomReqVO;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.interfaces.IUserRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import ystar.framework.web.starter.context.YStarRequestContext;
import ystar.living.Vo.resp.LivingRoomPageRespVO;
import ystar.living.Vo.resp.LivingRoomRespVO;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.interfaces.ILivingRoomRpc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    @DubboReference
    private ILivingRoomRpc iLivingRoomRpc;

    @DubboReference
    private IUserRpc iUserRpc;

    /**
     * 查询正在直播的列表
     * @param livingRoomReqVO
     * @return
     */
    @Override
    public LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO) {
        PageWrapper<LivingRoomRespDTO> resultPage = iLivingRoomRpc.list(BeanUtil.copyProperties(livingRoomReqVO, LivingRoomReqDTO.class));
        LivingRoomPageRespVO livingRoomPageRespVO = new LivingRoomPageRespVO();
        livingRoomPageRespVO.setList(ConvertBeanUtils.convertList(resultPage.getList(), LivingRoomRespVO.class));
        livingRoomPageRespVO.setHasNext(resultPage.isHasNext());
        return livingRoomPageRespVO;
    }

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
        livingRoomReqDTO.setRoomName("YStar官方直播间" + new Random().nextInt(10000));

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

        // UserDTO userDTO = userRpc.getUserById(userId);
        Map<Long, UserDTO> userDTOMap = iUserRpc.batchQueryUserInfo(Arrays.asList(respDTO.getAnchorId(), userId).stream().distinct().collect(Collectors.toList()));
        UserDTO anchor = userDTOMap.get(respDTO.getAnchorId());
        UserDTO watcher = userDTOMap.get(userId);

        LivingRoomInitVO respVO = new LivingRoomInitVO();
        respVO.setAnchorNickName(anchor.getNickName());
        respVO.setWatcherNickName(watcher.getNickName());
        respVO.setUserId(userId);

        respVO.setAvatar(StringUtils.isEmpty(anchor.getAvatar()) ? "https://cdn.acwing.com/media/user/profile/photo/461757_lg_ae724ef652.jpg" : anchor.getAvatar());
        respVO.setWatcherAvatar(watcher.getAvatar());

        if (respDTO.getAnchorId() == null || userId == null) {
            //直播间不存在，设置roomId为-1
            respVO.setRoomId(-1);
        }else {
            respVO.setRoomId(respDTO.getId());
            respVO.setAnchorId(respDTO.getAnchorId());
            respVO.setAnchor(respDTO.getAnchorId().equals(userId));
        }

        respVO.setDefaultBgImg("https://cdn.acwing.com/media/user/profile/photo/311396_lg_6c9d3a738b.jpg");
        return respVO;
    }
}
