package com.ystar.user.api.Controller;

import com.ystar.common.VO.WebResponseVO;
import com.ystar.user.api.Service.IHomePageService;
import com.ystar.user.api.Vo.HomePageVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ystar.framework.web.starter.context.YStarRequestContext;

@RestController
@RequestMapping("/home")
public class HomePageController {
    @Resource
    private IHomePageService homePageService;

    @PostMapping("/initPage")
    public WebResponseVO initPage() {
        Long userId = YStarRequestContext.getUserId();

        HomePageVO homePageVO = new HomePageVO();
        homePageVO.setLoginStatus(false);

        if (userId != null) {
            homePageVO = homePageService.initPage(userId);
            homePageVO.setLoginStatus(true);
        }

        return WebResponseVO.success(homePageVO);
    }
}