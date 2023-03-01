package cn.com.gffunds.pushmessage.controller;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.pushmessage.common.ReturnResult;
import cn.com.gffunds.pushmessage.service.RefreshService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author hezhc
 * @date 2023/2/14 8:53
 * @description redis推送订阅Controller
 */
@RestController
@RequestMapping("/redis_message")
@Slf4j
public class RedisMessageController {

    /**
     * for test 测试redis推送消息
     */
    @Resource
    private RedisTemplate irmRedisTemplate;

    @PostMapping("/send/irm")
    public String sendIrm(@RequestBody JsonNode jsonNode) {
        String channel = jsonNode.get("channel").asText();
        irmRedisTemplate.convertAndSend(channel, JacksonUtil.toJson(jsonNode.get("data")));
        return "success";
    }

    @Resource
    private RedisTemplate ipmRedisTemplate;

    @PostMapping("/send/ipm")
    public String sendIpm(@RequestBody JsonNode jsonNode) {
        String channel = jsonNode.get("channel").asText();
        ipmRedisTemplate.convertAndSend(channel, JacksonUtil.toJson(jsonNode.get("data")));
        return "success";
    }


    @Autowired
    private RefreshService refreshService;

    /**
     * 手动刷新redis channel
     */
    @GetMapping("/refresh")
    public ReturnResult<String> refresh() {
        refreshService.refresh();
        return new ReturnResult<>();
    }
}
