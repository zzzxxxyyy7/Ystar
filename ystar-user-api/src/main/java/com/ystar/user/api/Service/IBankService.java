package com.ystar.user.api.Service;

import com.ystar.user.api.Vo.PayProductReqVO;
import com.ystar.user.api.Vo.PayProductRespVO;
import com.ystar.user.api.Vo.PayProductVO;

public interface IBankService {

    /**
     * 查询相关产品信息
     */
    PayProductVO products(Integer type);

    /**
     * 发起支付
     */
    PayProductRespVO payProduct(PayProductReqVO payProductReqVO);
}