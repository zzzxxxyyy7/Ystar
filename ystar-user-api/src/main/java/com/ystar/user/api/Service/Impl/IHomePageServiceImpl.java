package com.ystar.user.api.Service.Impl;

import com.ystar.user.api.Service.IHomePageService;
import com.ystar.user.api.Vo.HomePageVO;
import com.ystar.user.constant.UserTagsEnum;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.interfaces.IUserRpc;
import com.ystar.user.interfaces.IUserTagRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class IHomePageServiceImpl implements IHomePageService {

    @DubboReference
    private IUserRpc userRpc;

    @DubboReference
    private IUserTagRpc userTagRpc;

    @Override
    public HomePageVO initPage(Long userId) {

        UserDTO userDTO = userRpc.getUserById(userId);
        System.out.println(userDTO);

        HomePageVO homePageVO = new HomePageVO();
        homePageVO.setLoginStatus(false);

        if (userId != null) {
            homePageVO.setAvatar(userDTO.getAvatar());
            homePageVO.setUserId(userDTO.getUserId());
            homePageVO.setNickName(userDTO.getNickName());

            //VIP用户才能开播
            homePageVO.setShowStartLivingBtn(userTagRpc.containTag(userId, UserTagsEnum.IS_VIP));
        }

        return homePageVO;
    }
}