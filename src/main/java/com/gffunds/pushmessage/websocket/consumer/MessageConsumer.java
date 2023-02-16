package com.gffunds.pushmessage.websocket.consumer;

import cn.hutool.extra.spring.SpringUtil;
import com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import com.gffunds.pushmessage.websocket.entity.Message;
import com.gffunds.pushmessage.websocket.entity.UserInfo;
import com.gffunds.pushmessage.websocket.handler.MessageHandler;
import com.gffunds.pushmessage.websocket.manager.BizMessageManager;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hezhc
 * @date 2023/2/14 15:06
 * @description 消息消费者，用于封装WebSocketSession
 */
@Data
@Accessors(chain = true)
@Slf4j
public class MessageConsumer {
    /** 是否有效(0-无效，1-有效) */
    private Integer valid;
    /** 用户信息 */
    private UserInfo userInfo;
    /** 客户端session */
    private WebSocketSession webSocketSession;
    /** 对应客户端的订阅数据 */
    private Map<String, BizMessageManager> bizMessageManagers;


    public MessageConsumer() {
        this.bizMessageManagers = new ConcurrentHashMap<>();
    }

    /**
     * 消费信息
     */
    @SneakyThrows
    public synchronized void consume(Message message) {
        // 获取业务
        String bizId = message.getBizId();
        String topic = message.getTopic();
        BizMessageManager bizMessageManager = bizMessageManagers.get(bizId);
        List<String> topics = bizMessageManager.getTopics();
        // todo 判断主题是否在该客户端订阅列表
        if (this.webSocketSession.isOpen()) {
            if (topics.contains(topic)) {
                // todo 重试机制，多次发送失败valid置为0
                this.webSocketSession.sendMessage(new TextMessage(String.valueOf(message)));
            }
            for (String subscribeTopic : topics) {
                if (subscribeTopic.contains("*") &&
                        topic.startsWith(subscribeTopic.replace("*", ""))) {
                    this.webSocketSession.sendMessage(new TextMessage(String.valueOf(message)));
                    return;
                }
            }
        } else {
            this.valid = WebSocketConstants.INVALID;
        }
    }


    /**
     * 订阅
     */
    public void subscribe(Map<String, BizMessageManager> bizMessageManagerMap) {
        this.bizMessageManagers = bizMessageManagerMap;
        Map<String, MessageHandler> dispatcherMap = getMessageDispatcher().getDispatcherMap();
        for (Map.Entry<String, BizMessageManager> entry : bizMessageManagerMap.entrySet()) {
            String bizId = entry.getKey();
            // 分发器获取messageHandler
            MessageHandler messageHandler = dispatcherMap.get(bizId);
            messageHandler.registerObserver(this);
        }
    }

    /**
     * 退订
     */
    public void unsubscribe(Map<String, BizMessageManager> bizMessageManagerMap) {
        Map<String, MessageHandler> dispatcherMap = getMessageDispatcher().getDispatcherMap();
        for (Map.Entry<String, BizMessageManager> entry : bizMessageManagerMap.entrySet()) {
            String bizId = entry.getKey();
            // 分发器获取messageHandler
            MessageHandler messageHandler = dispatcherMap.get(bizId);
            if (messageHandler != null) {
                messageHandler.removeObserver(this);
            }
        }
    }

    /**
     * 获取MessageDispatcher
     */
    private MessageDispatcher getMessageDispatcher() {
        return SpringUtil.getBean(MessageDispatcher.class);
    }

    /**
     * 关闭连接，置为不可用
     */
    @SneakyThrows
    public void closeConnection() {
        this.valid = WebSocketConstants.INVALID;
        if (this.webSocketSession != null && this.webSocketSession.isOpen()) {
            this.webSocketSession.close();
        }
    }

}
