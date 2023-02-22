package cn.com.gffunds.pushmessage.websocket.dispatcher;

import cn.com.gffunds.pushmessage.websocket.entity.Message;
import cn.com.gffunds.pushmessage.websocket.handler.MessageHandler;
import cn.com.gffunds.pushmessage.exception.PushMessageException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * @author hezhc
 * @date 2023/2/14 14:32
 * @description 消息分发器
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDispatcher {
    private Map<String, MessageHandler> dispatcherMap;

    /**
     * 分发器根据业务id分发到不同的消息处理器
     */
    @SneakyThrows
    public void doDispatch(Message message) {
        String bizId = message.getBizId();
        MessageHandler messageHandler = this.dispatcherMap.get(bizId);
        if (messageHandler == null) {
            throw new PushMessageException(String.format("分发器无法找到对应业务处理器！bizId=%s", bizId));
        }
        messageHandler.receiveMessage(message);
    }
}
