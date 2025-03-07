package cn.com.hzc.pushmessage.websocket.consumer;

import cn.com.hzc.pushmessage.common.enumeration.ErrCodeEnum;
import cn.com.hzc.pushmessage.config.LogConfig;
import cn.com.hzc.pushmessage.util.JacksonUtil;
import cn.com.hzc.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.hzc.pushmessage.websocket.entity.Message;
import cn.com.hzc.pushmessage.websocket.entity.MessageResponse;
import cn.com.hzc.pushmessage.websocket.entity.UserInfo;
import cn.com.hzc.pushmessage.websocket.handler.MessageHandler;
import cn.com.hzc.pushmessage.websocket.manager.BizMessageManager;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hezhc
 * @date 2023/2/14 15:06
 * @description 消息消费者，用于封装WebSocketSession
 */
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
@Component
@Scope("prototype")
public class MessageConsumer {
    /**
     * 是否有效
     */
    private AtomicBoolean valid = new AtomicBoolean(true);
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    /**
     * 客户端session
     */
    private WebSocketSession webSocketSession;
    /**
     * 对应客户端的订阅数据
     */
    private Map<String, BizMessageManager> bizMessageManagers;

    /**
     * 通配符匹配
     */
    private AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 心跳最后活跃时间
     */
    private Long lastActiveTime;


    /**
     * 创建时间
     */
    private String createTime = DateUtil.now();

    @Autowired
    private LogConfig logConfig;

    @Autowired
    private MessageDispatcher messageDispatcher;


    public MessageConsumer() {
        this.bizMessageManagers = new ConcurrentHashMap<>();
    }

    /**
     * 消费信息
     */
    @SneakyThrows
    public synchronized void consume(Message message) {
        try {
            if (this.webSocketSession.isOpen()) {
                if (isSubscribed(message)) {
                    ObjectNode node = JacksonUtil.getObjectMapper().convertValue(message, ObjectNode.class);
                    node.put("pushTime", DateUtil.now());
                    this.webSocketSession.sendMessage(new TextMessage(node.toString()));
                    if (logConfig.isLogEnable()) {
                        log.info("消息推送成功！用户：{}, 内容：{}", userInfo.getUsername(), node);
                    }
                }
            } else {
                this.closeConnection();
            }
        } catch (Exception e) {
            log.error("推送消息异常！", e);
            this.closeConnection();
        }
    }


    /**
     * 订阅
     */
    public void subscribe(Map<String, BizMessageManager> bizMessageManagerMap, MessageResponse response) {
        Map<String, MessageHandler> dispatcherMap = messageDispatcher.getDispatcherMap();
        // 把订阅的信息保存
        Set<String> bizIdSet = new HashSet<>();
        for (Map.Entry<String, BizMessageManager> entry : bizMessageManagerMap.entrySet()) {
            String bizId = entry.getKey();
            BizMessageManager value = entry.getValue();
            if (bizMessageManagers.containsKey(bizId)) {
                // 如果存在该业务就把主题添加至主题列表
                bizMessageManagers.get(bizId).addTopics(value.getTopics());
            } else {
                MessageHandler messageHandler = dispatcherMap.get(bizId);
                if (messageHandler == null) {
                    bizIdSet.add(bizId);
                    continue;
                }
                // 注册到messageHandler
                bizMessageManagers.put(bizId, value);
                // 注册到业务处理器
                messageHandler.registerObserver(this);
            }
            log.info("用户[{}]订阅 业务：{} 主题：{}", userInfo.getUsername(), bizId, value.getTopics());
        }
        if (!CollectionUtils.isEmpty(bizIdSet)) {
            String msg = String.format("客户端订阅的以下业务不存在，请检查命令请求！bizId=%s", bizIdSet);
            if (bizMessageManagerMap.size() == bizIdSet.size()) {
                response.setCode(ErrCodeEnum.REST_EXCEPTION.code()).setData(msg);
            } else {
                response.setCode(ErrCodeEnum.PARTIAL_INCORRECT.code()).setData(msg);
            }
            log.warn(msg);
        }
    }

    /**
     * 退订
     */
    public void unsubscribe(Map<String, BizMessageManager> bizMessageManagerMap, MessageResponse response) {
        Map<String, MessageHandler> dispatcherMap = messageDispatcher.getDispatcherMap();
        Set<String> bizIdSet = new HashSet<>();
        for (Map.Entry<String, BizMessageManager> entry : bizMessageManagerMap.entrySet()) {
            String bizId = entry.getKey();
            BizMessageManager value = entry.getValue();
            // 判断业务处理列表是否有该业务
            if (bizMessageManagers.containsKey(bizId)) {
                BizMessageManager bizMessageManager = bizMessageManagers.get(bizId);
                // 移除该业务下的topics
                Set<String> removeTopics = bizMessageManager.removeTopics(value.getTopics());
                log.info("用户[{}]取消订阅 业务：{} 主题：{}", userInfo.getUsername(), bizId, removeTopics);
                // 该业务订阅列表为空
                if (bizMessageManager.isEmpty()) {
                    bizMessageManagers.remove(bizId);
                    MessageHandler messageHandler = dispatcherMap.get(bizId);
                    if (messageHandler != null) {
                        messageHandler.removeObserver(this);
                    }
                }
            } else {
                bizIdSet.add(bizId);
            }
        }
        if (!CollectionUtils.isEmpty(bizIdSet)) {
            String msg = String.format("客户端退订的以下业务不存在，请检查命令请求！bizId=%s", bizIdSet);
            log.warn(msg);
            if (bizMessageManagerMap.size() == bizIdSet.size()) {
                response.setCode(ErrCodeEnum.REST_EXCEPTION.code()).setData(msg);
            } else {
                response.setCode(ErrCodeEnum.PARTIAL_INCORRECT.code()).setData(msg);
            }
        }
    }


    /**
     * 关闭连接，置为不可用
     */
    @SneakyThrows
    public synchronized void closeConnection() {
        this.valid.set(false);
        this.bizMessageManagers = null;
        this.userInfo = null;
        if (this.webSocketSession != null && this.webSocketSession.isOpen()) {
            this.webSocketSession.close();
        }
    }

    /**
     * 判断是否可用
     */
    public boolean isValid() {
        return this.valid.get();
    }


    /**
     * 判断该消费者是否订阅该信息
     */
    public boolean isSubscribed(Message message) {
        // 获取业务
        String bizId = message.getBizId();
        String topic = message.getTopic();
        BizMessageManager bizMessageManager = bizMessageManagers.get(bizId);
        Set<String> topics = bizMessageManager.getTopics();
        if (topics.contains(topic)) {
            return true;
        }
        //   ant通配符匹配 判断主题是否在该客户端订阅列表
        for (String subscribeTopic : topics) {
            if (matcher.match(subscribeTopic, topic)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移除客户端的订阅数据
     */
    public void removeBiz(String bizId) {
        this.bizMessageManagers.remove(bizId);
    }
}
