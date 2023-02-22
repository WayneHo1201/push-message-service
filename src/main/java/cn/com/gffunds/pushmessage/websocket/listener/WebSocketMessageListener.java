package cn.com.gffunds.pushmessage.websocket.listener;

/**
 * @author hezhc
 * @date 2023/2/14 14:26
 * @description websocket消息监听器接口
 */
public interface WebSocketMessageListener {
    void handleMessage(String bizId, String topic, Object msg, String receiveTime);
}
