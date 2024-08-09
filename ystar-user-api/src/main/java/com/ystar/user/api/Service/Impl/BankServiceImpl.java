package com.ystar.user.api.Service.Impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ystar.user.api.Service.IBankService;
import com.ystar.user.api.Vo.PayProductItemVO;
import com.ystar.user.api.Vo.PayProductReqVO;
import com.ystar.user.api.Vo.PayProductRespVO;
import com.ystar.user.api.Vo.PayProductVO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ystar.framework.web.starter.Error.BizBaseErrorEnum;
import ystar.framework.web.starter.Error.ErrorAssert;
import ystar.framework.web.starter.context.YStarRequestContext;
import ystar.live.bank.constants.OrderStatusEnum;
import ystar.live.bank.constants.PaySourceEnum;
import ystar.live.bank.dto.PayOrderDTO;
import ystar.live.bank.dto.PayProductDTO;
import ystar.live.bank.interfaces.IPayOrderRpc;
import ystar.live.bank.interfaces.IPayProductRpc;
import ystar.live.bank.interfaces.YStarCurrencyAccountRpc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class BankServiceImpl implements IBankService {
    
    @DubboReference
    private IPayProductRpc payProductRpc;

    @DubboReference
    private YStarCurrencyAccountRpc yStarCurrencyAccountRpc;

    @DubboReference
    private IPayOrderRpc payOrderRpc;

    @Resource
    private RestTemplate restTemplate;

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
        payProductVO.setCurrentBalance(yStarCurrencyAccountRpc.getBalance(YStarRequestContext.getUserId()));
        payProductVO.setPayProductItemVOList(payProductItemVOS);
        return payProductVO;
    }

    @Override
    public PayProductRespVO payProduct(PayProductReqVO payProductReqVO) {

        // 参数校验
        ErrorAssert.isTure(payProductReqVO != null && payProductReqVO.getProductId() != null && payProductReqVO.getPaySource() != null, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isNotNull(PaySourceEnum.find(payProductReqVO.getPaySource()), BizBaseErrorEnum.PARAM_ERROR);

        // 查询payProductDTO
        PayProductDTO payProductDTO = payProductRpc.getByProductId(payProductReqVO.getProductId());
        ErrorAssert.isNotNull(payProductDTO, BizBaseErrorEnum.PARAM_ERROR);

        // 生成一条订单（待支付状态）
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setProductId(payProductReqVO.getProductId());
        payOrderDTO.setUserId(YStarRequestContext.getUserId());
        payOrderDTO.setPayTime(new Date());
        payOrderDTO.setSource(payProductReqVO.getPaySource());
        payOrderDTO.setPayChannel(payProductReqVO.getPayChannel());
        String orderId = payOrderRpc.insertOne(payOrderDTO);
        // 模拟点击 去支付 按钮，更新订单状态为 支付中
        payOrderRpc.updateOrderStatus(orderId, OrderStatusEnum.PAYING.getCode());
        PayProductRespVO payProductRespVO = new PayProductRespVO();
        payProductRespVO.setOrderId(orderId);

        // TODO 这里应该是支付成功后吗，由第三方支付所做的事情，因为我们是模拟支付，所以我们直接发起支付成功后的回调请求：
        com.alibaba.fastjson2.JSONObject jsonObject = new JSONObject();
        jsonObject.put("orderId", orderId);
        jsonObject.put("userId", YStarRequestContext.getUserId());
        jsonObject.put("bizCode", 10001);
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("param", jsonObject.toJSONString());

        // 使用RestTemplate进行HTTP的发送
        ResponseEntity<String> resultEntity = restTemplate.postForEntity("http://localhost:8201/live/bank/payNotify/wxNotify?param={param}", null, String.class, paramMap);
        System.out.println(resultEntity.getBody());

        return payProductRespVO;
    }
}