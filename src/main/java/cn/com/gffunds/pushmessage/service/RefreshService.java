package cn.com.gffunds.pushmessage.service;

import cn.com.gffunds.pushmessage.config.DefaultRedisProperties;
import cn.com.gffunds.pushmessage.config.IpmRedisProperties;
import cn.com.gffunds.pushmessage.config.IrmRedisProperties;
import cn.com.gffunds.pushmessage.config.SubscribeConfig;
import cn.com.gffunds.pushmessage.listener.AbstractRedisMessageListener;
import cn.com.gffunds.pushmessage.listener.IpmRedisMessageListener;
import cn.com.gffunds.pushmessage.listener.IrmRedisMessageListener;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import cn.hutool.extra.spring.SpringUtil;
import com.liferay.portal.kernel.util.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hezhc
 * @date 2023/2/27 9:42
 * @description 刷新配置
 */
@Service
public class RefreshService {
    @Autowired
    private MessageDispatcher messageDispatcher;
    @Autowired
    private SubscribeConfig subscribeConfig;
    @Resource
    private IrmRedisMessageListener irmRedisMessageListener;
    @Resource
    private IpmRedisMessageListener ipmRedisMessageListener;
    @Autowired
    private IrmRedisProperties irmRedisProperties;
    @Autowired
    private IpmRedisProperties ipmRedisProperties;

    /**
     * 刷新所有配置
     */
    @SuppressWarnings("unchecked")
    public void refresh(){
        RedisMessageListenerContainer irmContainer = SpringUtil.getBean("irmRedisMessageListenerContainer", RedisMessageListenerContainer.class);
        RedisMessageListenerContainer ipmContainer = SpringUtil.getBean("ipmRedisMessageListenerContainer", RedisMessageListenerContainer.class);
        redisConfigRefresh(irmContainer, irmRedisMessageListener, irmRedisProperties);
        redisConfigRefresh(ipmContainer, ipmRedisMessageListener, ipmRedisProperties);
        Set<String> bizIdSet = ListUtil.concat(irmRedisProperties.getSubscribes(), ipmRedisProperties.getSubscribes()).stream().map(BizTopic::getBizId).collect(Collectors.toSet());
        dispatcherRefresh(bizIdSet);
    }

    /**
     * 刷新redis配置
     */
    private void redisConfigRefresh(RedisMessageListenerContainer container, AbstractRedisMessageListener listener, DefaultRedisProperties redisProperties) {
        Set<Topic> subscribes = subscribeConfig.generateTopics(redisProperties);
        container.removeMessageListener(listener);
        container.addMessageListener(listener, subscribes);
    }

    /**
     * 刷新分发器
     */
    private void dispatcherRefresh(Set<String> bizIdSet) {
        Map<String, MessageHandler> dispatcherMap = messageDispatcher.getDispatcherMap();
        for (String bizId : bizIdSet) {
            if (!dispatcherMap.containsKey(bizId)) {
                dispatcherMap.put(bizId, new MessageHandler().setBizId(bizId));
            }
        }
        dispatcherMap.entrySet().removeIf(entry -> !bizIdSet.contains(entry.getKey()));
    }


}
