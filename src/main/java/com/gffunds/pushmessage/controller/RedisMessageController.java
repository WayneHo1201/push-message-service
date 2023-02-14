package com.gffunds.pushmessage.controller;

import com.gffunds.pushmessage.common.ReturnResult;
import com.gffunds.pushmessage.config.RedisSubConfig;
import com.gffunds.pushmessage.listener.RedisMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/14 8:53
 * @description redis推送订阅Controller
 */
@RestController("redis_message")
@Slf4j
public class RedisMessageController {

    /**
     * for test 测试redis推送消息
     */
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/")
    public String send(@RequestParam String channel) {
        //TODO 执行主业务
        redisTemplate.convertAndSend(channel, "for test");
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
    private RedisSubConfig redisSubConfig;

    /**
     * 手动刷新redis channel
     */
    @GetMapping("/refresh")
    public ReturnResult<String> refresh() {
        container.removeMessageListener(listener);
        List<String> subscribes = redisSubConfig.getSubscribes();
        for (String subscribe : subscribes) {
            container.addMessageListener(listener, new ChannelTopic(subscribe));
        }
        String msg = String.format("刷新redis订阅配置！[%s]", String.join(",", subscribes));
        return new ReturnResult<>(msg);
    }

}
