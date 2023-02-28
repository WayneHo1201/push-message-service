package cn.com.gffunds.pushmessage.listener;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * redis消息监听器
 */
@Component
public class IpmRedisMessageListener extends AbstractRedisMessageListener {

    @Resource
    private RedisTemplate ipmRedisTemplate;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message redisMessage, byte[] pattern) {
        super.messageListen(ipmRedisTemplate, redisMessage, "ipm");
    }
}