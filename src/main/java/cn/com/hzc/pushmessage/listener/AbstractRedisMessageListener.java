package cn.com.hzc.pushmessage.listener;

import cn.com.hzc.pushmessage.util.JacksonUtil;
import cn.com.hzc.pushmessage.websocket.common.enumeration.MsgTypeEnum;
import cn.com.hzc.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.hzc.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.hzc.pushmessage.websocket.listener.WebSocketMessageListener;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hezhc
 * @date 2023/2/24 19:24
 * @description 消息监听抽象类(子类在启动的时候自动注入并放入IOC容器)
 */
@Slf4j
public class AbstractRedisMessageListener implements MessageListener, WebSocketMessageListener {


    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 每个监听器都需要构造自己的方法
    }

    public void messageListen(RedisTemplate<String, Object> redisTemplate, Message redisMessage, String sourceId) {
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
        logMap.put("消息源", sourceId);
        logMap.put("频道", channel);
        logMap.put("消息内容", msg);
        logMap.put("接收时间", receiveTime);
        log.info("=======================================redis消息监听器=======================================");
        log.info(JacksonUtil.toJson(logMap));
        // 根据separator分割获取业务id和topic
        String[] channelSplit = channel.split(WebSocketConstants.SEPARATOR);
        String bizId = channelSplit[0];
        String topic = channelSplit[1];
        handleMessage(bizId, topic, msg, sourceId, receiveTime);
    }

    @Override
    public void handleMessage(String bizId, String topic, Object msg, String sourceId, String receiveTime) {
        cn.com.hzc.pushmessage.websocket.entity.Message message = new cn.com.hzc.pushmessage.websocket.entity.Message()
                .setBizId(bizId)
                .setData(msg)
                .setTopic(topic)
                .setMsgType(MsgTypeEnum.MESSAGE.code())
                .setSourceId(sourceId)
                .setReceiveTime(receiveTime);
        //  推送到分发器
        MessageDispatcher messageDispatcher = SpringUtil.getBean("messageDispatcher", MessageDispatcher.class);
        messageDispatcher.doDispatch(message);
    }
}
