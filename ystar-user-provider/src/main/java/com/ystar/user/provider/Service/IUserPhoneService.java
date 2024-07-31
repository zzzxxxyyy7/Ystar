package com.ystar.user.provider.Service;

import com.ystar.user.dto.UserLoginDTO;
import com.ystar.user.dto.UserPhoneDTO;
import com.ystar.user.provider.Domain.po.TUserPhonePo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Rhss
* @description 针对表【t_user_phone_00】的数据库操作Service
* @createDate 2024-07-31 20:21:40
*/
public interface IUserPhoneService extends IService<TUserPhonePo> {

    UserLoginDTO login(String phone);

    UserPhoneDTO queryByPhone(String phone);

    List<UserPhoneDTO> queryByUserId(Long userId);
}
