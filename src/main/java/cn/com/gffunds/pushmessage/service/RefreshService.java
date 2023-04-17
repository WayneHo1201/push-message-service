package cn.com.gffunds.pushmessage.service;

import cn.com.gffunds.pushmessage.config.DefaultRedisProperties;
import cn.com.gffunds.pushmessage.config.IrmRedisProperties;
import cn.com.gffunds.pushmessage.config.SubscribeConfig;
import cn.com.gffunds.pushmessage.listener.AbstractRedisMessageListener;
import cn.com.gffunds.pushmessage.listener.IrmRedisMessageListener;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Resource
    private IrmRedisMessageListener irmRedisMessageListener;
    @Autowired
    private IrmRedisProperties irmRedisProperties;
    @Resource
    private RedisMessageListenerContainer irmRedisMessageListenerContainer;

    /**
     * 刷新所有配置
     */
    @SuppressWarnings("unchecked")
    public void refresh() {
        log.info("=====================订阅配置刷新开始======================");
        redisConfigRefresh(irmRedisMessageListenerContainer, irmRedisMessageListener, irmRedisProperties);
        List<BizTopic> list = new ArrayList<>(irmRedisProperties.getSubscribes());
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
        dispatcherMap.entrySet().removeIf(entry -> !bizIdSet.contains(entry.getKey()));
        log.info("分发器刷新完成！");
    }
}
