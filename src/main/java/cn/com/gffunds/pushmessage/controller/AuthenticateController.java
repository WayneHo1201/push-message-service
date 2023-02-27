package cn.com.gffunds.pushmessage.controller;

import cn.com.gffunds.pushmessage.common.ReturnResult;
import cn.com.gffunds.pushmessage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hezhc
 * @date 2023/2/27 9:56
 * @description 鉴权控制器
 */
@RestController
public class AuthenticateController {
    @Autowired
    private UserService userService;

    @GetMapping("/gettoken")
    public ReturnResult<String> authorize(@RequestParam String sessionId) throws Exception {
        String token = userService.authorize(sessionId);
        return new ReturnResult<>(token);
    }
}
