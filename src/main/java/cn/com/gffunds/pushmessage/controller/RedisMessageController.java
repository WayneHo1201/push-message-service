package cn.com.gffunds.pushmessage.controller;

import cn.com.gffunds.pushmessage.common.ReturnResult;
import cn.com.gffunds.pushmessage.config.SubscribeConfig;
import cn.com.gffunds.pushmessage.listener.RedisMessageListener;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import cn.com.gffunds.commons.json.JacksonUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

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
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/send")
    public String send(@RequestBody JsonNode jsonNode) {
        String channel = jsonNode.get("channel").asText();
        redisTemplate.convertAndSend(channel, JacksonUtil.toJson(jsonNode.get("data")));
        return "success";
    }

    @Autowired
    private RedisMessageListenerContainer container;

    @Autowired
    private RedisMessageListener listener;

    /**
     * 订阅channel
     *
     * @param channel redis channel
     */
    @GetMapping("/subscribe")
    public ReturnResult<String> subscribe(@RequestParam String channel) {
        container.addMessageListener(listener, new ChannelTopic(channel));
        String msg = String.format("订阅频道[%s]成功", channel);
        return new ReturnResult<>(msg);
    }

    /**
     * 退订channel
     *
     * @param channel redis channel
     */
    @GetMapping("/cancel")
    public ReturnResult<String> cancel(@RequestParam String channel) {
        container.removeMessageListener(listener, new ChannelTopic(channel));
        String msg = String.format("退订频道[%s]！", channel);
        return new ReturnResult<>(msg);
    }

    @Autowired
    private SubscribeConfig subscribeConfig;

    /**
     * 手动刷新redis channel
     */
    @GetMapping("/refresh")
    public ReturnResult<String> refresh() {
        container.removeMessageListener(listener);
        Set<String> subscribes = new ConcurrentHashSet<>();
        for (BizTopic redisSubscribes : subscribeConfig.getRedis()) {
            String bizId = redisSubscribes.getBizId();
            subscribes.addAll(redisSubscribes.getTopics()
                    .stream()
                    .map(topic -> bizId + WebSocketConstants.SEPARATOR + topic)
                    .collect(Collectors.toSet()));
        }
        for (String subscribe : subscribes) {
            container.addMessageListener(listener, new ChannelTopic(subscribe));
        }
        String msg = String.format("刷新redis订阅配置！[%s]", String.join(",", subscribes));
        return new ReturnResult<>(msg);
    }

}
