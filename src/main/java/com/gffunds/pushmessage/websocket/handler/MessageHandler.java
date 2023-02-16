package com.gffunds.pushmessage.websocket.handler;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import com.gffunds.pushmessage.websocket.consumer.MessageConsumer;
import com.gffunds.pushmessage.websocket.entity.Message;
import lombok.Data;

import java.util.Iterator;
import java.util.Set;

/**
 * @author hezhc
 * @date 2023/2/14 16:40
 * @description 消息处理器，用于处理某个业务
 */
@Data
public class MessageHandler {
    private String bizId;
    private Set<MessageConsumer> consumerSet;

    public MessageHandler(){
        this.consumerSet = new ConcurrentHashSet<>();
    }

    /**
     * 消费者注册到订阅列表
     */
    public void registerObserver(MessageConsumer messageConsumer) {
        consumerSet.add(messageConsumer);
    }

    /**
     * 从订阅列表移除该消费者
     */
    public void removeObserver(MessageConsumer messageConsumer) {
        consumerSet.remove(messageConsumer);
    }

    /**
     * 通知合法订阅者
     */
    public void notifyObservers(Message message) {
        Iterator<MessageConsumer> iterator = consumerSet.iterator();
        while (iterator.hasNext()) {
            MessageConsumer messageConsumer = iterator.next();
            if (messageConsumer.getValid() == WebSocketConstants.INVALID) {
                iterator.remove();
                continue;
            }
            messageConsumer.consume(message);
        }
    }

    /**
     * 接收消息源信息
     */
    public void receiveMessage(Message message) {
        this.notifyObservers(message);
    }
}
