package cn.com.gffunds.pushmessage.controller;

import cn.com.gffunds.pushmessage.common.ReturnResult;
import cn.com.gffunds.pushmessage.service.RefreshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hezhc
 * @date 2023/3/7 10:04
 * @description 刷新配置
 */
@RestController
@RequestMapping("/refresh")
public class RefreshController {

    @Autowired
    private RefreshService refreshService;

    /**
     * 手动刷新redis订阅退订配置
     */
    @GetMapping("/redis")
    public ReturnResult<String> refresh() {
        refreshService.refresh();
        return new ReturnResult<>();
    }
}
