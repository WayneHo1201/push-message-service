package cn.com.gffunds.pushmessage.websocket.consumer;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.entity.Message;
import cn.com.gffunds.pushmessage.websocket.entity.MessageResponse;
import cn.com.gffunds.pushmessage.websocket.entity.UserInfo;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import cn.com.gffunds.pushmessage.websocket.manager.BizMessageManager;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
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
     * 是否有效(0-无效，1-有效)
     */
    private AtomicBoolean valid;
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
     * 重试次数
     */
    @Value("${websocket.retry-times:3}")
    private int retryTimes;
    /**
     * 睡眠时间
     */
    @Value("${websocket.sleep-millis:3000}")
    private int sleepMillis;

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
        // 获取业务
        String bizId = message.getBizId();
        String topic = message.getTopic();
        BizMessageManager bizMessageManager = bizMessageManagers.get(bizId);
        Set<String> topics = bizMessageManager.getTopics();
        if (this.webSocketSession.isOpen()) {
            if (topics.contains(topic)) {
                //  重试机制，多次发送失败valid置为0
                sendMessage(message, retryTimes);
                return;
            }
            //  判断主题是否在该客户端订阅列表
            for (String subscribeTopic : topics) {
                if (subscribeTopic.contains("*") && (
                        topic.startsWith(subscribeTopic.replace("*", "")) ||
                        topic.endsWith(subscribeTopic.replace("*", "")))) {
                    //  重试机制，多次发送失败valid置为0
                    sendMessage(message, retryTimes);
                    return;
                }
            }
        } else {
            this.closeConnection();
        }
    }


    /**
     * 重试发送
     */
    @SneakyThrows
    private void sendMessage(Message message, int retry) {
        boolean flag = false;
        retry++;
        while (!flag && retry-- > 0) {
            try {
                this.webSocketSession.sendMessage(new TextMessage(JacksonUtil.toJson(message)));
                flag = true;
            } catch (IOException e) {
                log.error(String.format("消息发送失败！将会重试 %d 次", retry), e);
                Thread.sleep(sleepMillis);
            }
        }
        if (!flag) {
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
                bizMessageManagers.get(bizId).getTopics().addAll(value.getTopics());
            } else {
                // 注册到messageHandler
                bizMessageManagers.put(bizId, value);
                MessageHandler messageHandler = dispatcherMap.get(bizId);
                if (messageHandler == null) {
                    bizIdSet.add(bizId);
                    continue;
                }
                messageHandler.registerObserver(this);
            }
        }
        if (!CollectionUtils.isEmpty(bizIdSet)) {
            String msg = String.format("客户端订阅的以下业务不存在，请检查命令请求！bizId=%s", bizIdSet);
            log.warn(msg);
            response.setSuccess(WebSocketConstants.MSG_FAIL).setData(msg);
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
            if (bizMessageManagers.containsKey(bizId)) {
                Set<String> topics = bizMessageManagers.get(bizId).getTopics();
                topics.removeAll(value.getTopics());
                // 该业务订阅列表为空
                if (CollectionUtils.isEmpty(topics)) {
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
            response.setSuccess(WebSocketConstants.MSG_FAIL).setData(msg);
        }
    }



    /**
     * 关闭连接，置为不可用
     */
    @SneakyThrows
    public synchronized void closeConnection() {
        this.setValid(WebSocketConstants.INVALID);
        this.bizMessageManagers = null;
        this.userInfo = null;
        if (this.webSocketSession != null && this.webSocketSession.isOpen()) {
            this.webSocketSession.close();
        }
    }
}
