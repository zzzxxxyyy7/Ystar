package com.ystar.user.api.Controller;

import com.ystar.user.constant.UserTagsEnum;
import com.ystar.user.interfaces.IUserTagRpc;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/tag")
public class UserTagController {

    @DubboReference
    private IUserTagRpc iUserTagRpc;

    @PostMapping("/setTag")
    public Boolean setTag(@RequestParam Long userId) {
        return iUserTagRpc.setTag(userId , UserTagsEnum.IS_SVIP);
    }

    @PostMapping("/canalTag")
    public Boolean canalTag(@RequestParam Long userId) {
        return iUserTagRpc.cancelTag(userId , UserTagsEnum.IS_SVIP);
    }

    @GetMapping("/containTag")
    public Boolean containTag(@RequestParam Long userId) {
        return iUserTagRpc.containTag(userId , UserTagsEnum.IS_SVIP);
    }

}
