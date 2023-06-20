package cn.com.hzc.pushmessage.config;

import cn.com.hzc.pushmessage.listener.AbstractRedisMessageListener;
import cn.com.hzc.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.hzc.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.hzc.pushmessage.websocket.entity.BizTopic;
import cn.com.hzc.pushmessage.websocket.handler.MessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

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

        /**
     * 构造分发器（若有新数据源接入需要新增构建逻辑）
     */
    @Bean
    public MessageDispatcher messageDispatcher(SourceProperties sourceProperties) {
        Map<String, MessageHandler> map = new ConcurrentHashMap<>();
        List<BizTopic> bizTopics = new ArrayList<>();
        // 把配置读取的订阅信息加载到bizTopics
        for (SourceProperties.RedisProperties redisProperty : sourceProperties.getRedis()) {
            bizTopics.addAll(redisProperty.getSubscribes());
        }
        for (BizTopic redisSubscribes : bizTopics) {
            String bizId = redisSubscribes.getBizId();
            // 创建业务消息处理器
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
    public RedisMessageListenerContainer generateRedisMessageListenerContainer(AbstractRedisMessageListener listener
            , RedisConnectionFactory redisConnectionFactory, DefaultRedisProperties redisProperties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        Set<Topic> subscribes = generateTopics(redisProperties);
        container.addMessageListener(listener, subscribes);
        container.afterPropertiesSet();
        return container;
    }

    /**
     * 构造订阅信息
     */
    public Set<Topic> generateTopics(DefaultRedisProperties redisProperties) {
        //订阅频道 这个container可以添加多个messageListener
        Set<Topic> topicSet = new HashSet<>();
        for (BizTopic redisSubscribes : redisProperties.getSubscribes()) {
            String bizId = redisSubscribes.getBizId();
            // 读取配置加载到主题列表
            topicSet.addAll(redisSubscribes.getTopics()
                    .stream()
                    .map(topic -> new PatternTopic(bizId + WebSocketConstants.SEPARATOR + topic))
                    .collect(Collectors.toSet()));
        }
        return topicSet;
    }
}