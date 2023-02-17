package com.gffunds.pushmessage.listener;

import cn.hutool.core.date.DateUtil;
import com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import com.gffunds.pushmessage.websocket.entity.Message;
import com.gffunds.pushmessage.websocket.listener.WebSocketMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * redis消息监听器
 */
@Component
@Slf4j
public class RedisMessageListener implements MessageListener, WebSocketMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MessageDispatcher messageDispatcher;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message redisMessage, byte[] pattern) {
        // 获取消息
        byte[] messageBody = redisMessage.getBody();
        // 使用值序列化器转换
        Object msg = redisTemplate.getValueSerializer().deserialize(messageBody);
        // 获取监听的频道
        byte[] channelByte = redisMessage.getChannel();
        // 使用字符串序列化器转换
        String channel = String.valueOf(redisTemplate.getStringSerializer().deserialize(channelByte));
        String receiveTime = DateUtil.now();
        // 渠道名称转换
        log.info("\n===========redis消息监听器=============" +
                 "\n频道: " + channel +
                 "\n消息内容: " + msg +
                 "\n接收时间: " + receiveTime);
        // 根据separator分割获取业务id和topic
        String[] channelSplit = channel.split(WebSocketConstants.SEPARATOR);
        String bizId = channelSplit[0];
        String topic = channelSplit[1];
        handleMessage(bizId, topic, msg, receiveTime);
    }

    @Override
    public void handleMessage(String bizId, String topic, Object msg, String receiveTime) {
        Message message = new Message()
                .setBizId(bizId)
                .setData(msg)
                .setTopic(topic)
                .setMsgType(WebSocketConstants.MSG_TYPE_NORMAL)
                .setReceiveTime(receiveTime);
        //  推送到分发器
        messageDispatcher.doDispatch(message);
    }
}