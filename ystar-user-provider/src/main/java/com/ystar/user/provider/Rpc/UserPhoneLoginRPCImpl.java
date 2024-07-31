package com.ystar.user.provider.Rpc;

import com.ystar.user.dto.UserLoginDTO;
import com.ystar.user.dto.UserPhoneDTO;
import com.ystar.user.interfaces.IUserPhoneRPC;
import com.ystar.user.provider.Service.IUserPhoneService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
public class UserPhoneLoginRPCImpl implements IUserPhoneRPC {

    @Resource
    private IUserPhoneService iUserPhoneService;

    /**
     * 用户登录
     * @param phone
     * @return
     */
    @Override
    public UserLoginDTO login(String phone) {
        return iUserPhoneService.login(phone);
    }

    @Override
    public UserPhoneDTO queryByPhone(String phone) {
        return iUserPhoneService.queryByPhone(phone);
    }

    @Override
    public List<UserPhoneDTO> queryByUserId(Long userId) {
        return iUserPhoneService.queryByUserId(userId);
    }
}
