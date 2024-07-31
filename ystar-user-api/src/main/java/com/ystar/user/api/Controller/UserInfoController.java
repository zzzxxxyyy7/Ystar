package com.ystar.user.api.Controller;

import com.ystar.id.generate.interfaces.IdGenerateRpc;
import com.ystar.user.dto.UserDTO;
import com.ystar.user.interfaces.IUserRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserInfoController {
    
    @DubboReference
    private IUserRpc userRpc;

    @DubboReference
    private IdGenerateRpc idGenerateRpc;
    
    @GetMapping("/getUserInfo")
    public UserDTO dubbo(@RequestParam Long userId) {
        return userRpc.getUserById(userId);
    }

    @PostMapping("/updateUserInfo")
    public boolean getUserInfo(@RequestParam Long userId , @RequestParam String nickname) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName(nickname);
        return userRpc.updateUserInfo(userDTO);
    }

    @PostMapping("insertOne")
    public boolean insertUser(@RequestParam String nickName) {
        UserDTO userDTO = new UserDTO();
        // 用户注册所用参数为1
        userDTO.setUserId(idGenerateRpc.getSeqId(1));
        userDTO.setNickName(nickName);
        userDTO.setSex(1);
        return userRpc.insertOne(userDTO);
    }

    @GetMapping("/batchQueryUserInfo")
    public Map<Long, UserDTO> batchQueryUserInfo(@RequestParam String userIdStr) {
        return userRpc.batchQueryUserInfo(Arrays.stream(userIdStr.split(",")).map(Long::valueOf).collect(Collectors.toList()));
    }


}