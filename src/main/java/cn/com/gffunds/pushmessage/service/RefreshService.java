package cn.com.gffunds.pushmessage.service;

import cn.com.gffunds.pushmessage.config.DefaultRedisProperties;
import cn.com.gffunds.pushmessage.listener.AbstractRedisMessageListener;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import cn.hutool.core.collection.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hezhc
 * @date 2023/2/27 9:42
 * @description todo 刷新配置
 */
@Service
public class RefreshService {
    @Autowired
    private MessageDispatcher messageDispatcher;
    /**
     * 刷新配置
     */
    public void redisConfigRefresh(RedisMessageListenerContainer container, AbstractRedisMessageListener listener, DefaultRedisProperties redisProperties) {
        container.removeMessageListener(listener);
        Set<String> subscribes = new ConcurrentHashSet<>();
        for (BizTopic redisSubscribes : redisProperties.getSubscribes()) {
            String bizId = redisSubscribes.getBizId();
            subscribes.addAll(redisSubscribes.getTopics()
                    .stream()
                    .map(topic -> bizId + WebSocketConstants.SEPARATOR + topic)
                    .collect(Collectors.toSet()));
            Map<String, MessageHandler> map = messageDispatcher.getDispatcherMap();
            if (!map.containsKey(bizId)) {
                MessageHandler messageHandler = new MessageHandler();
                messageHandler.setBizId(bizId);
                map.put(bizId, messageHandler);
            }
        }
        for (String subscribe : subscribes) {
            container.addMessageListener(listener, new ChannelTopic(subscribe));
        }
    }
}
