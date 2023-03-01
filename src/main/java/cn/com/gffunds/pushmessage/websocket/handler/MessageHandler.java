package cn.com.gffunds.pushmessage.websocket.handler;

import cn.com.gffunds.pushmessage.util.SendMessageThreadPool;
import cn.com.gffunds.pushmessage.websocket.consumer.MessageConsumer;
import cn.com.gffunds.pushmessage.websocket.entity.Message;
import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.Set;

/**
 * @author hezhc
 * @date 2023/2/14 16:40
 * @description 消息处理器，用于处理某个业务
 */
@Getter
@Setter
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
        Set<MessageConsumer> messageConsumers = new ConcurrentHashSet<>();
        for (MessageConsumer consumer : consumerSet) {
            if (!consumer.equals(messageConsumer)) {
                messageConsumers.add(consumer);
            }
        }
        this.consumerSet = messageConsumers;
    }

    /**
     * 通知合法订阅者
     */
    public void notifyObservers(Message message) {
        Iterator<MessageConsumer> iterator = consumerSet.iterator();
        boolean flag = false;
        while (iterator.hasNext()) {
            MessageConsumer messageConsumer = iterator.next();
            if (!messageConsumer.isValid()) {
                flag = true;
                continue;
            }
            SendMessageThreadPool.runAsyncNoException(() -> messageConsumer.consume(message));
        }
        if (flag) {
            removeInvalidObserver();
        }

    }

    public synchronized void removeInvalidObserver() {
        consumerSet.removeIf(consumer -> !consumer.isValid());
    }

    /**
     * 接收消息源信息
     */
    public void receiveMessage(Message message) {
        this.notifyObservers(message);
    }
}
