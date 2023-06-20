package cn.com.hzc.pushmessage.websocket.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author hezhc
 * @date 2023/2/15 8:52
 * @description 订阅信息结构
 */
@Data
@Accessors(chain = true)
public class Message {
    /** 消息类型，普通消息、命令响应 */
    private String msgType;
    /** 消息源 */
    private String sourceId;
    /** 业务id */
    private String bizId;
    /** 消息主题 */
    private String topic;
    /** 消息体 */
    private Object data;
    /** 消息接收时间 */
    private String receiveTime;
}
