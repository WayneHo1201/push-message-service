package cn.com.hzc.pushmessage.websocket.common.enumeration;

/**
 * 业务信息枚举类
 */
public enum MsgTypeEnum {
    /**
     * 命令响应
     */
    COMMAND("command", "命令响应"),
    /**
     * 业务消息
     */
    MESSAGE("biz", "业务消息"),
    ;

    private final String  code;
    private final String  msg;

    MsgTypeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String msg() {
        return msg;
    }

    public String code() {
        return code;
    }
}
