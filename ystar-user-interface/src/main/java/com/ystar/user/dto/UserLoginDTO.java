package com.ystar.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4290788036479984698L;

    private boolean isLoginStatus;

    private String desc;

    private Long userId;

    public static UserLoginDTO LoginSuccess(Long userId) {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setLoginStatus(true);
        userLoginDTO.setUserId(userId);
        return userLoginDTO;
    }

    public static UserLoginDTO LoginError(String desc) {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setDesc(desc);
        userLoginDTO.setLoginStatus(false);
        return userLoginDTO;
    }
}
