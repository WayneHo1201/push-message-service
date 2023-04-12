package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.listener.AbstractRedisMessageListener;
import cn.com.gffunds.pushmessage.listener.IrmRedisMessageListener;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 订阅配置
 */
@Configuration
@Data
@Slf4j
public class SubscribeConfig {

    @Autowired
    private IrmRedisProperties irmRedisProperties;
    @Resource
    private RedisConnectionFactory irmRedisConnectionFactory;

    /**
     * 订阅redis
     */
    @Bean("irmRedisMessageListenerContainer")
    public RedisMessageListenerContainer irmRedisMessageListenerContainer(IrmRedisMessageListener listener) {
        return generateRedisMessageListenerContainer(listener, irmRedisConnectionFactory, irmRedisProperties);
    }

    /**
     * 构造分发器（若有新数据源接入需要新增构建逻辑）
     */
    @Bean
    public MessageDispatcher messageDispatcher() {
        Map<String, MessageHandler> map = new ConcurrentHashMap<>();
        List<BizTopic> redis = new ArrayList<>();
        redis.addAll(irmRedisProperties.getSubscribes());
        for (BizTopic redisSubscribes : redis) {
            String bizId = redisSubscribes.getBizId();
            MessageHandler messageHandler = new MessageHandler();
            messageHandler.setBizId(bizId);
            map.put(bizId, messageHandler);
        }
        log.info("===========消息分发器构造完成===========");
        return new MessageDispatcher(map);
    }


    /**
     * 构造container
     */
    private RedisMessageListenerContainer generateRedisMessageListenerContainer(AbstractRedisMessageListener listener
            , RedisConnectionFactory redisConnectionFactory, DefaultRedisProperties redisProperties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        Set<Topic> subscribes = generateTopics(redisProperties);
        container.addMessageListener(listener, subscribes);
        return container;
    }

    /**
     * 构造订阅信息
     */
    public Set<Topic> generateTopics(DefaultRedisProperties redisProperties) {
        //订阅频道 这个container可以添加多个messageListener
        Set<Topic> subscribes = new HashSet<>();
        for (BizTopic redisSubscribes : redisProperties.getSubscribes()) {
            String bizId = redisSubscribes.getBizId();
            subscribes.addAll(redisSubscribes.getTopics()
                    .stream()
                    .map(topic -> new PatternTopic(bizId + WebSocketConstants.SEPARATOR + topic))
                    .collect(Collectors.toSet()));
        }
        return subscribes;
    }
}