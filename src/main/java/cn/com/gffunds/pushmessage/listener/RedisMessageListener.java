package cn.com.gffunds.pushmessage.listener;

import cn.hutool.extra.spring.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.signature.qual.SignatureUnknown;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * redis消息监听器
 */
@AllArgsConstructor
@NoArgsConstructor
public class RedisMessageListener extends AbstractRedisMessageListener {

    private String id;

//    @Resource
//    private RedisTemplate<String, Object> irmRedisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(org.springframework.data.redis.connection.Message redisMessage, byte[] pattern) {
        RedisTemplate<String, Object> redisTemplate = SpringUtil.getBean(id + "RedisTemplate", RedisTemplate.class);
        super.messageListen(redisTemplate, redisMessage, id);
    }
}