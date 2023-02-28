package cn.com.gffunds.pushmessage.listener;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.listener.WebSocketMessageListener;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hezhc
 * @date 2023/2/24 19:24
 * @description
 */
@Slf4j
public class AbstractRedisMessageListener implements MessageListener, WebSocketMessageListener {
    @Autowired
    private MessageDispatcher messageDispatcher;


    @Override
    public void onMessage(Message message, byte[] pattern) {

    }

    public void messageListen(RedisTemplate redisTemplate, Message redisMessage, String messageSource) {
        // 获取消息
        byte[] messageBody = redisMessage.getBody();
        // 使用值序列化器转换
        Object msg = redisTemplate.getValueSerializer().deserialize(messageBody);
        // 获取监听的频道
        byte[] channelByte = redisMessage.getChannel();
        // 使用字符串序列化器转换
        String channel = String.valueOf(redisTemplate.getStringSerializer().deserialize(channelByte));
        String receiveTime = DateUtil.now();
        Map<String, Object> logMap = new LinkedHashMap<>();
        // 渠道名称转换
        logMap.put("消息源", messageSource);
        logMap.put("频道", channel);
        logMap.put("消息内容", msg);
        logMap.put("接收时间", receiveTime);
        log.info("=======================================redis消息监听器=======================================");
        log.info(JacksonUtil.toJson(logMap));
        // 根据separator分割获取业务id和topic
        String[] channelSplit = channel.split(WebSocketConstants.SEPARATOR);
        String bizId = channelSplit[0];
        String topic = channelSplit[1];
        handleMessage(bizId, topic, msg, messageSource, receiveTime);
    }

    @Override
    public void handleMessage(String bizId, String topic, Object msg, String messageSource, String receiveTime) {
        cn.com.gffunds.pushmessage.websocket.entity.Message message = new cn.com.gffunds.pushmessage.websocket.entity.Message()
                .setBizId(bizId)
                .setData(msg)
                .setTopic(topic)
                .setMsgType(WebSocketConstants.MSG_TYPE_NORMAL)
                .setMessageSource(messageSource)
                .setReceiveTime(receiveTime);
        //  推送到分发器
        messageDispatcher.doDispatch(message);
    }
}
