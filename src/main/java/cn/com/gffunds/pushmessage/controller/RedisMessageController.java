package cn.com.gffunds.pushmessage.controller;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.pushmessage.client.WebSocketClient;
import cn.com.gffunds.pushmessage.websocket.entity.MessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RedisTemplate irmRedisTemplate;

    @PostMapping("/send/irm")
    public String sendIrm(@RequestBody JsonNode jsonNode) {
        String channel = jsonNode.get("channel").asText();
        irmRedisTemplate.convertAndSend(channel, JacksonUtil.toJson(jsonNode.get("data")));
        return "success";
    }

    @Resource
    private RedisTemplate ipmRedisTemplate;

    /**
     * 测试redis推送消息
     */
    @PostMapping("/send/ipm")
    public String sendIpm(@RequestBody JsonNode jsonNode) {
        String channel = jsonNode.get("channel").asText();
        ipmRedisTemplate.convertAndSend(channel, JacksonUtil.toJson(jsonNode.get("data")));
        return "success";
    }

    /**
     * 发送订阅退订信息
     */
    @Autowired
    private WebSocketClient webSocketClient;

    @PostMapping("/send")
    public String send(@RequestBody MessageRequest request) {
        webSocketClient.send(JacksonUtil.toJson(request));
        return "success";
    }
}
