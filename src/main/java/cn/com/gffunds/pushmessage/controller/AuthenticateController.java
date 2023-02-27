package cn.com.gffunds.pushmessage.controller;

import cn.com.gffunds.pushmessage.common.ReturnResult;
import cn.com.gffunds.pushmessage.entity.SessionIdDTO;
import cn.com.gffunds.pushmessage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author hezhc
 * @date 2023/2/27 9:56
 * @description 鉴权控制器
 */
@RestController
public class AuthenticateController {
    @Autowired
    private UserService userService;

    @PostMapping("/authorize")
    public ReturnResult<String> authorize(@Valid @RequestBody SessionIdDTO sessionIdDTO) throws Exception {
        String token = userService.authorize(sessionIdDTO.getSessionId());
        return new ReturnResult<>(token);
    }
}
