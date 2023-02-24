package cn.com.gffunds.pushmessage.listener;

import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.dispatcher.MessageDispatcher;
import cn.com.gffunds.pushmessage.websocket.entity.Message;
import cn.com.gffunds.pushmessage.websocket.listener.WebSocketMessageListener;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * redis消息监听器
 */
@Component
@Slf4j
public class IrmRedisMessageListener extends AbstractRedisMessageListener {

    @Resource
    private RedisTemplate irmRedisTemplate;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message redisMessage, byte[] pattern) {
        // 获取消息
        byte[] messageBody = redisMessage.getBody();
        // 使用值序列化器转换
        Object msg = irmRedisTemplate.getValueSerializer().deserialize(messageBody);
        // 获取监听的频道
        byte[] channelByte = redisMessage.getChannel();
        // 使用字符串序列化器转换
        String channel = String.valueOf(irmRedisTemplate.getStringSerializer().deserialize(channelByte));
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
}