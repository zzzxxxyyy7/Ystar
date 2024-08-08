package com.ystar.user.api.Service;

import com.ystar.user.api.Vo.GiftConfigVO;
import com.ystar.user.api.Vo.GiftReqVO;

import java.util.List;

public interface IGiftService {
    
    List<GiftConfigVO> listGift();
    
    boolean send(GiftReqVO giftReqVO);
}