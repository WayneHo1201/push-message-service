package cn.com.gffunds.pushmessage.listener;

import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * redis消息监听器
 */
@Component
@Slf4j
public class IpmRedisMessageListener extends AbstractRedisMessageListener {

    @Resource
    private RedisTemplate ipmRedisTemplate;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message redisMessage, byte[] pattern) {
        // 获取消息
        byte[] messageBody = redisMessage.getBody();
        // 使用值序列化器转换
        Object msg = ipmRedisTemplate.getValueSerializer().deserialize(messageBody);
        // 获取监听的频道
        byte[] channelByte = redisMessage.getChannel();
        // 使用字符串序列化器转换
        String channel = String.valueOf(ipmRedisTemplate.getStringSerializer().deserialize(channelByte));
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