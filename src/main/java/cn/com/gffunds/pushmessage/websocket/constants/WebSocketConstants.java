package cn.com.gffunds.pushmessage.websocket.constants;

/**
 * @author hezhc
 * @date 2023/2/14 15:06
 * @description websocket常量
 */
public class WebSocketConstants {
    /** 用户属性key */
    public static final String ATTR_USER = "user";
    /** 订阅 */
    public static final String SUBSCRIBE = "subscribe";
    /** 退订 */
    public static final String UNSUBSCRIBE = "unSubscribe";
    /** 分割符 */
    public static final String SEPARATOR = ":";
    /** 消息类型  */
    public static final String MSG_TYPE_NORMAL = "0";
    /** 命令类型 */
    public static final String MSG_TYPE_COMMAND = "1";
    /** 合法 */
    public static final int VALID = 1;
    /** 不合法 */
    public static final int INVALID = 0;
    /** sessionId请求头 */
    public static final String SESSION_ID_HEADER = "Session-Id";
    /** sessionId */
    public static final String SESSION_ID = "sessionId";
    /** sso鉴权路径 */
    public static final String AUTHORIZATION_URL = "/sso/authorize";
    /** 订阅退订信息 */
    public static final String PAYLOAD = "payload";

}
