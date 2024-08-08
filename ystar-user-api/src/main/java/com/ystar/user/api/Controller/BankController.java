package com.ystar.user.api.Controller;

import com.ystar.common.VO.WebResponseVO;
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
    
    @PostMapping("/products")
    public WebResponseVO products(Integer type) {
        ErrorAssert.isNotNull(type, BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(bankService.products(type)); 
    }
}