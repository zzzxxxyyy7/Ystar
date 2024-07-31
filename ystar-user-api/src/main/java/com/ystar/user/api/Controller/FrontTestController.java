package com.ystar.user.api.Controller;

import com.ystar.user.api.Vo.TestVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class FrontTestController {

    @PostMapping("/testPost")
    public String testPost(String id) {
        System.out.println("id is " + id);
        return "success";
    }

    @PostMapping("testVo")
    public String testVo(TestVo vo) {
        System.out.println(vo.toString());
        return "success";
    }
}
