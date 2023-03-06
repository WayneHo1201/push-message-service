package cn.com.gffunds.pushmessage.websocket.common.enumeration;

/**
 * 命令枚举类
 */
public enum WebsocketCommandEnum {
    /**
     * 订阅
     */
    SUBSCRIBE("subscribe", "订阅"),
    /**
     * 退订
     */
    UNSUBSCRIBE("unSubscribe", "退订"),
    /**
     * 心跳包
     */
    PING("ping", "心跳"),
    ;

    private String code;
    private String msg;

    WebsocketCommandEnum(String code, String msg) {
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
