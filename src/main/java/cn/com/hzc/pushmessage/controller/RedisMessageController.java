package cn.com.hzc.pushmessage.controller;

import cn.com.hzc.pushmessage.common.ReturnResult;
import cn.com.hzc.pushmessage.entity.PushDTO;
import cn.com.hzc.pushmessage.websocket.constants.WebSocketConstants;
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

    @PostMapping("/push")
    public ReturnResult push(@RequestBody PushDTO pushDTO) {
        String bizId = pushDTO.getBizId();
        String topic = pushDTO.getTopic();
        String channel = bizId + WebSocketConstants.SEPARATOR + topic;
        defaultRestTemplate.convertAndSend(channel, pushDTO.getData());
        log.info("发送信息成功! channel={}", channel);
        return new ReturnResult<>();
    }
}
