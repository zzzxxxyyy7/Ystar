package com.ystar.user.api.Controller;

import com.ystar.common.VO.WebResponseVO;
import com.ystar.user.api.Service.IBankService;
import com.ystar.user.api.Vo.PayProductReqVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ystar.framework.web.starter.Error.BizBaseErrorEnum;
import ystar.framework.web.starter.Error.ErrorAssert;

@RestController
@RequestMapping("/bank")
public class BankController {
    
    @Resource
    private IBankService bankService;

    /**
     * 查询产品列表
     * @param type
     * @return
     */
    @PostMapping("/products")
    public WebResponseVO products(Integer type) {
        ErrorAssert.isNotNull(type, BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(bankService.products(type)); 
    }

    // 1.申请调用第三方支付接口（签名-》支付宝/微信）（生成一条支付中状态的订单）
    // 2.生成一个（特定的支付页）二维码（输入账户密码，支付）（第三方平台完成）
    // 3.发送回调请求-》业务方
    // 要求（可以接收不同平台的回调数据）
    // 可以根据业务标识去回调不同的业务服务（自定义参数组成中，塞入一个业务code,根据业务code去回调不同的业务服务）
    @PostMapping("/payProduct")
    public WebResponseVO payProduct(PayProductReqVO payProductReqVO) {
        return WebResponseVO.success(bankService.payProduct(payProductReqVO));
    }
}