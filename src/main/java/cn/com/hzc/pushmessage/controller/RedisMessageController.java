package cn.com.hzc.pushmessage.controller;

import cn.com.hzc.pushmessage.util.JacksonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author hezhc
 * @date 2023/2/14 8:53
 * @description for test redis推送订阅Controller
 */
@RestController
@RequestMapping("/redis_message")
@Slf4j
public class RedisMessageController {

    /**
     * 测试redis推送消息
     */
    @Resource
    private RedisTemplate defaultRestTemplate;

    @PostMapping("/send")
    public String sendIrm(@RequestBody JsonNode jsonNode) {
        String channel = jsonNode.get("channel").asText();
        defaultRestTemplate.convertAndSend(channel, JacksonUtil.toJson(jsonNode.get("data")));
        return "success";
    }
}
