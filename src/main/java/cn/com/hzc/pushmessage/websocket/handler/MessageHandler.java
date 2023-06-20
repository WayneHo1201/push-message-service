package cn.com.hzc.pushmessage.websocket.handler;

import cn.com.hzc.pushmessage.constants.MDCConstants;
import cn.com.hzc.pushmessage.util.SendMessageThreadPool;
import cn.com.hzc.pushmessage.websocket.consumer.MessageConsumer;
import cn.com.hzc.pushmessage.websocket.entity.Message;
import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Set;
import java.util.UUID;

/**
 * @author hezhc
 * @date 2023/2/14 16:40
 * @description 消息处理器，用于处理某个业务
 */
@Getter
@Setter
@Slf4j
@Accessors(chain = true)
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
        boolean flag = false;
        for (MessageConsumer messageConsumer : consumerSet) {
            // 判断该客户端是否可用
            if (!messageConsumer.isValid()) {
                flag = true;
                continue;
            }
            // 异步通知消费者
            SendMessageThreadPool.runAsyncNoException(() -> messageConsumer.consume(message));
        }
        // 若存在不可用的客户端就调用移除方法
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
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //设置traceId值
        MDC.put(MDCConstants.TRACE_ID, uuid);
        this.notifyObservers(message);
    }
}
