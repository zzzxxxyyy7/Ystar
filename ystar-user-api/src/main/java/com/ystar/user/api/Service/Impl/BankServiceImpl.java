package com.ystar.user.api.Service.Impl;

import com.alibaba.fastjson2.JSON;
import com.ystar.user.api.Service.IBankService;
import com.ystar.user.api.Vo.PayProductItemVO;
import com.ystar.user.api.Vo.PayProductVO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import ystar.framework.web.starter.context.YStarRequestContext;
import ystar.live.bank.dto.PayProductDTO;
import ystar.live.bank.interfaces.IPayProductRpc;
import ystar.live.bank.interfaces.YStarCurrencyAccountRpc;

import java.util.ArrayList;
import java.util.List;

@Service
public class BankServiceImpl implements IBankService {
    
    @DubboReference
    private IPayProductRpc payProductRpc;

    @DubboReference
    private YStarCurrencyAccountRpc qiyuCurrencyAccountRpc;

    @Override
    public PayProductVO products(Integer type) {
        List<PayProductDTO> payProductDTOS = payProductRpc.products(type);
        List<PayProductItemVO> payProductItemVOS = new ArrayList<>();

        for (PayProductDTO payProductDTO : payProductDTOS) {
            PayProductItemVO payProductItemVO = new PayProductItemVO();
            payProductItemVO.setId(payProductDTO.getId());
            payProductItemVO.setName(payProductDTO.getName());
            payProductItemVO.setCoinNum(JSON.parseObject(payProductDTO.getExtra()).getInteger("coin"));
            payProductItemVOS.add(payProductItemVO);
        }

        PayProductVO payProductVO = new PayProductVO();
        payProductVO.setCurrentBalance(qiyuCurrencyAccountRpc.getBalance(YStarRequestContext.getUserId()));
        payProductVO.setPayProductItemVOList(payProductItemVOS);
        return payProductVO;
    }
}