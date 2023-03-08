package cn.com.gffunds.pushmessage.websocket.entity;

import cn.com.gffunds.pushmessage.common.enumeration.ErrCodeEnum;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
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
    private String msgType = WebSocketConstants.MSG_TYPE_COMMAND;
    /** 消息id */
    private String msgId;
    /** 是否成功 */
    private String code = ErrCodeEnum.SUCCESS.code();
    /** 消息体 */
    private String data = ErrCodeEnum.SUCCESS.msg();
    /** 是否成功 */
    public boolean isSuccess() {
        return ErrCodeEnum.SUCCESS.code().equals(code);
    }
}
