package cn.com.gffunds.pushmessage.listener;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * redis消息监听器
 */
@Component
public class IrmRedisMessageListener extends AbstractRedisMessageListener {

    @Resource
    private RedisTemplate<String, Object> irmRedisTemplate;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message redisMessage, byte[] pattern) {
        super.messageListen(irmRedisTemplate, redisMessage, "irm");
    }
}