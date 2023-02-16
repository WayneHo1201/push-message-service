package com.gffunds.pushmessage.config;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import com.gffunds.pushmessage.listener.RedisMessageListener;
import com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import com.gffunds.pushmessage.websocket.entity.BizTopic;
import com.gffunds.pushmessage.websocket.handler.MessageHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 订阅配置
 */
@ConfigurationProperties(prefix = "subscribes")
@Configuration
@RefreshScope
@Data
public class SubscribeConfig {

    /** 消息中心订阅redis数据源的订阅列表 */
    private List<BizTopic> redis;

    /**
     * 订阅redis
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory factory, RedisMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        //订阅频道 这个container可以添加多个messageListener
        Set<String> subscribes = new ConcurrentHashSet<>();
        for (BizTopic redisSubscribes : redis) {
            String bizId = redisSubscribes.getBizId();
            subscribes.addAll(redisSubscribes.getTopics()
                    .stream()
                    .map(topic -> bizId + WebSocketConstants.SEPARATOR + topic)
                    .collect(Collectors.toSet()));
        }
        for (String subscribe : subscribes) {
            container.addMessageListener(listener, new ChannelTopic(subscribe));
        }
        return container;
    }

    /**
     * 构造分发器（若有新数据源接入需要新增构建逻辑）
     */
    @Bean
    public MessageDispatcher messageDispatcher() {
        Map<String, MessageHandler> map = new ConcurrentHashMap<>();
        for (BizTopic redisSubscribes : redis) {
            String bizId = redisSubscribes.getBizId();
            MessageHandler messageHandler = new MessageHandler();
            messageHandler.setBizId(bizId);
            map.put(bizId, messageHandler);
        }
        return new MessageDispatcher(map);
    }

}