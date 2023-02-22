package cn.com.gffunds.pushmessage.websocket.consumer;

import cn.com.gffunds.pushmessage.websocket.entity.Message;
import cn.com.gffunds.pushmessage.websocket.entity.UserInfo;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import cn.com.gffunds.pushmessage.websocket.manager.BizMessageManager;
import cn.com.gffunds.commons.json.JacksonUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
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
    /** 客户端session  */
    private WebSocketSession webSocketSession;
    /** 对应客户端的订阅数据 */
    private Map<String, BizMessageManager> bizMessageManagers;
    /** 重试次数 */
    private int retryTimes;
    /** 睡眠时间 */
    private int sleepMillis;

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
            // todo 判断主题是否在该客户端订阅列表
            for (String subscribeTopic : topics) {
                if (subscribeTopic.contains("*") &&
                        topic.startsWith(subscribeTopic.replace("*", ""))) {
                    //  重试机制，多次发送失败valid置为0
                    sendMessage(message, retryTimes);
                    return;
                }
            }
        } else {
            this.valid = WebSocketConstants.INVALID;
        }
    }


    /**
     * 重试发送
     */
    @SneakyThrows
    private void sendMessage(Message message, int retry) {
        boolean flag = false;
        retry ++;
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
            this.valid = WebSocketConstants.INVALID;
        }
    }


    /**
     * 订阅
     */
    public void subscribe(Map<String, BizMessageManager> bizMessageManagerMap) {
        Map<String, MessageHandler> dispatcherMap = getMessageDispatcher().getDispatcherMap();
        // 把订阅的信息保存
        for (Map.Entry<String, BizMessageManager> entry : bizMessageManagerMap.entrySet()) {
            String bizId = entry.getKey();
            BizMessageManager value = entry.getValue();
            if (bizMessageManagers.containsKey(bizId)) {
                bizMessageManagers.get(bizId).getTopics().addAll(value.getTopics());
            } else {
                // 注册到messageHandler
                bizMessageManagers.put(bizId, value);
                MessageHandler messageHandler = dispatcherMap.get(bizId);
                messageHandler.registerObserver(this);
            }
        }
    }

    /**
     * 退订
     */
    public void unsubscribe(Map<String, BizMessageManager> bizMessageManagerMap) {
        Map<String, MessageHandler> dispatcherMap = getMessageDispatcher().getDispatcherMap();
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
                    messageHandler.removeObserver(this);
                }
            } else {
                log.warn("客户端订阅列表不存在该业务，退订失败！bizId={}", bizId);
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
        this.bizMessageManagers = null;
        this.userInfo = null;
        if (this.webSocketSession != null && this.webSocketSession.isOpen()) {
            this.webSocketSession.close();
        }
    }

}
