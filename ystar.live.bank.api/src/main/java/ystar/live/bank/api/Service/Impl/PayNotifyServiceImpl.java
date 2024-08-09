package ystar.live.bank.api.Service.Impl;

import com.alibaba.fastjson2.JSON;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import ystar.live.bank.api.Service.IPayNotifyService;
import ystar.live.bank.api.Vo.WxPayNotifyVO;
import ystar.live.bank.dto.PayOrderDTO;
import ystar.live.bank.interfaces.IPayOrderRpc;

@Service
public class PayNotifyServiceImpl implements IPayNotifyService {
    
    @DubboReference
    private IPayOrderRpc payOrderRpc;

    @Override
    public String notifyHandler(String paramJson) {
        WxPayNotifyVO wxPayNotifyVO = JSON.parseObject(paramJson, WxPayNotifyVO.class);
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setUserId(wxPayNotifyVO.getUserId());
        payOrderDTO.setOrderId(wxPayNotifyVO.getOrderId());
        payOrderDTO.setBizCode(wxPayNotifyVO.getBizCode());
        return payOrderRpc.payNotify(payOrderDTO) ? "success" : "fail";
    }
}