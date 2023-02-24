package cn.com.gffunds.pushmessage.listener;

import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.listener.WebSocketMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * @author hezhc
 * @date 2023/2/24 19:24
 * @description
 */
public class AbstractRedisMessageListener implements MessageListener, WebSocketMessageListener {
    @Autowired
    private MessageDispatcher messageDispatcher;


    @Override
    public void onMessage(Message message, byte[] pattern) {

    }

    @Override
    public void handleMessage(String bizId, String topic, Object msg, String receiveTime) {
        cn.com.gffunds.pushmessage.websocket.entity.Message message = new cn.com.gffunds.pushmessage.websocket.entity.Message()
                .setBizId(bizId)
                .setData(msg)
                .setTopic(topic)
                .setMsgType(WebSocketConstants.MSG_TYPE_NORMAL)
                .setReceiveTime(receiveTime);
        //  推送到分发器
        messageDispatcher.doDispatch(message);
    }
}
