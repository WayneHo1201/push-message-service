package cn.com.gffunds.pushmessage.websocket.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author hezhc
 * @date 2023/2/15 8:55
 * @description 命令信息返回
 */
@Data
@Accessors(chain = true)
public class MessageResponse {
    /** 消息类型，普通消息、命令响应 */
    private String msgType;
    /** 消息id */
    private String msgId;
    /** 消息体 */
    private String data;
}
