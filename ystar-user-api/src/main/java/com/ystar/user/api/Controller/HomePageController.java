package com.ystar.user.api.Controller;

import com.ystar.common.VO.WebResponseVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ystar.framework.web.starter.context.YStarRequestContext;

@RestController
@RequestMapping("/home")
public class HomePageController {

    @PostMapping("/initPage")
    public WebResponseVO initPage() {
        Long userId = YStarRequestContext.getUserId();
        System.out.println(userId);
        //前端调用 initPage --> success 状态则代表已经登录过，token有效，前端可隐藏登录按钮
        return WebResponseVO.success();
    }
}