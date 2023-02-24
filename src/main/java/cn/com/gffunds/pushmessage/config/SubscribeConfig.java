package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.listener.AbstractRedisMessageListener;
import cn.com.gffunds.pushmessage.listener.IpmRedisMessageListener;
import cn.com.gffunds.pushmessage.listener.IrmRedisMessageListener;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Autowired
    private IpmRedisProperties ipmRedisProperties;
    @Resource
    private RedisConnectionFactory irmRedisConnectionFactory;
    @Resource
    private RedisConnectionFactory ipmRedisConnectionFactory;

    /**
     * 订阅redis
     */
    @Bean("irmRedisMessageListenerContainer")
    public RedisMessageListenerContainer irmRedisMessageListenerContainer(IrmRedisMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(irmRedisConnectionFactory);
        //订阅频道 这个container可以添加多个messageListener
        Set<String> subscribes = new ConcurrentHashSet<>();
        for (BizTopic redisSubscribes : irmRedisProperties.getSubscribes()) {
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
     * 订阅redis
     */
    @Bean("ipmRedisMessageListenerContainer")
    public RedisMessageListenerContainer ipmRedisMessageListenerContainer(IpmRedisMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(ipmRedisConnectionFactory);
        //订阅频道 这个container可以添加多个messageListener
        Set<String> subscribes = new ConcurrentHashSet<>();
        for (BizTopic redisSubscribes : ipmRedisProperties.getSubscribes()) {
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
        List<BizTopic> redis = new ArrayList<>();
        redis.addAll(ipmRedisProperties.getSubscribes());
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

}