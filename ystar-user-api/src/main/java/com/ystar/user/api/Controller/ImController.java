package com.ystar.user.api.Controller;

import com.ystar.common.VO.WebResponseVO;
import com.ystar.user.api.Service.ImService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/im")
public class ImController {

    @Resource
    private ImService imService;

    @PostMapping("/getImConfig")
    public WebResponseVO getImConfig() {
        return WebResponseVO.success(imService.getImConfig());
    }
}