package cn.com.gffunds.pushmessage.controller;

import cn.com.gffunds.commons.json.JacksonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


//    /**
//     * 手动刷新redis channel
//     */
//    @GetMapping("/refresh")
//    public ReturnResult<String> refresh() {
//        container.removeMessageListener(listener);
//        Set<String> subscribes = new ConcurrentHashSet<>();
//        for (BizTopic redisSubscribes : subscribeConfig.getRedis()) {
//            String bizId = redisSubscribes.getBizId();
//            subscribes.addAll(redisSubscribes.getTopics()
//                    .stream()
//                    .map(topic -> bizId + WebSocketConstants.SEPARATOR + topic)
//                    .collect(Collectors.toSet()));
//        }
//        for (String subscribe : subscribes) {
//            container.addMessageListener(listener, new ChannelTopic(subscribe));
//        }
//        String msg = String.format("刷新redis订阅配置！[%s]", String.join(",", subscribes));
//        return new ReturnResult<>(msg);
//    }

}
