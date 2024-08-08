package com.ystar.user.api.Service.Impl;

import com.ystar.common.utils.ConvertBeanUtils;
import com.ystar.user.api.Service.IGiftService;
import com.ystar.user.api.Vo.GiftConfigVO;
import com.ystar.user.api.Vo.GiftReqVO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ystar.gift.dto.GiftConfigDTO;
import ystar.gift.interfaces.IGiftConfigRpc;

import java.util.List;

@Service
public class GiftServiceImpl implements IGiftService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiftServiceImpl.class);

    @DubboReference
    private IGiftConfigRpc giftConfigRpc;

    @Override
    public List<GiftConfigVO> listGift() {
        List<GiftConfigDTO> giftConfigDTOList = giftConfigRpc.queryGiftList();
        return ConvertBeanUtils.convertList(giftConfigDTOList, GiftConfigVO.class);
    }

    @Override
    public boolean send(GiftReqVO giftReqVO) {
        // TODO 待实现
        return false;
    }
}