package com.ystar.user.provider.Rpc;

import com.ystar.user.dto.UserDTO;
import com.ystar.user.interfaces.IUserRpc;
import com.ystar.user.provider.Service.IUserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserRpcImpl implements IUserRpc {

    @Resource
    private IUserService iUserService;

    @Override
    public UserDTO getUserById(Long userId) {
        return iUserService.getUserById(userId);
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        return iUserService.updateUserInfo(userDTO);
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        return iUserService.insertOne(userDTO);
    }
}
