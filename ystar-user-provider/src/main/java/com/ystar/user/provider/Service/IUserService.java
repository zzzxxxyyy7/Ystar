package com.ystar.user.provider.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.provider.Domain.po.UserPO;

public interface IUserService extends IService<UserPO> {

    /**
     * 根据用户id进行查询
     * @param userId
     * @return
     */
    UserDTO getUserById(Long userId);

    /**
     * 更新用户信息
     * @param userDTO
     * @return
     */
    boolean updateUserInfo(UserDTO userDTO);

    /**
     * 插入用户
     * @param userDTO
     * @return
     */
    boolean insertOne(UserDTO userDTO);
    
}