package cn.com.hzc.pushmessage.controller;

import cn.com.hzc.pushmessage.common.ReturnResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/greet")
    public ReturnResult hello(){
        return new ReturnResult<>();
    }
}
