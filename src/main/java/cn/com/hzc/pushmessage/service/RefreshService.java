package cn.com.hzc.pushmessage.service;

import cn.com.hzc.pushmessage.config.DefaultRedisProperties;
import cn.com.hzc.pushmessage.config.SourceProperties;
import cn.com.hzc.pushmessage.config.SubscribeConfig;
import cn.com.hzc.pushmessage.listener.AbstractRedisMessageListener;
import cn.com.hzc.pushmessage.listener.RedisMessageListener;
import cn.com.hzc.pushmessage.websocket.consumer.MessageConsumer;
import cn.com.hzc.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.hzc.pushmessage.websocket.entity.BizTopic;
import cn.com.hzc.pushmessage.websocket.handler.MessageHandler;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hezhc
 * @date 2023/2/27 9:42
 * @description 刷新配置
 */
@Service
@Slf4j
public class RefreshService {
    @Autowired
    private MessageDispatcher messageDispatcher;
    @Autowired
    private SubscribeConfig subscribeConfig;
    @Autowired
    private SourceProperties sourceProperties;

    /**
     * 刷新所有配置
     */
    @SuppressWarnings("unchecked")
    public void refresh() {
        log.info("=====================订阅配置刷新开始======================");
        List<BizTopic> list = new ArrayList<>();
        for (SourceProperties.RedisProperties redisProperty : sourceProperties.getRedis()) {
            String id = redisProperty.getId();
            String containerName = id + "RedisMessageListenerContainer";
            String listenerName = id + "RedisMessageListener";
            RedisMessageListenerContainer container = SpringUtil.getBean(containerName, RedisMessageListenerContainer.class);
            RedisMessageListener listener = SpringUtil.getBean(listenerName, RedisMessageListener.class);
            redisConfigRefresh(container, listener, redisProperty);
            list.addAll(redisProperty.getSubscribes());
        }
        Set<String> bizIdSet = list.stream().map(BizTopic::getBizId).collect(Collectors.toSet());
        dispatcherRefresh(bizIdSet);
        log.info("=====================订阅配置刷新结束======================");
    }

    /**
     * 刷新redis配置
     */
    private void redisConfigRefresh(RedisMessageListenerContainer container, AbstractRedisMessageListener listener, DefaultRedisProperties redisProperties) {
        Set<Topic> subscribes = subscribeConfig.generateTopics(redisProperties);
        log.info("刷新后配置={}", subscribes);
        container.removeMessageListener(listener);
        container.addMessageListener(listener, subscribes);
        log.info("监听器刷新完成！");
    }

    /**
     * 刷新分发器
     */
    private void dispatcherRefresh(Set<String> bizIdSet) {
        Map<String, MessageHandler> dispatcherMap = messageDispatcher.getDispatcherMap();
        for (String bizId : bizIdSet) {
            dispatcherMap.putIfAbsent(bizId, new MessageHandler().setBizId(bizId));
        }
        // 找到所有订阅该业务的客户端
        Iterator<Map.Entry<String, MessageHandler>> iterator = dispatcherMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MessageHandler> entry = iterator.next();
            MessageHandler messageHandler = entry.getValue();
            if (!bizIdSet.contains(messageHandler.getBizId())) {
                log.info("消息推送中心取消订阅业务[{}]", messageHandler.getBizId());
                Set<MessageConsumer> consumerSet = messageHandler.getConsumerSet();
                for (MessageConsumer messageConsumer : consumerSet) {
                    // 从客户端列表中移除该业务的订阅
                    messageConsumer.removeBiz(messageHandler.getBizId());
                }
                iterator.remove();
            }
        }
        log.info("分发器刷新完成！");
    }
}
