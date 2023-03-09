package cn.com.gffunds.pushmessage.websocket.constants;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hezhc
 * @date 2023/2/14 15:06
 * @description websocket常量
 */
public class WebSocketConstants {
    /** 用户属性key */
    public static final String ATTR_USER = "user";
    /** 分割符 */
    public static final String SEPARATOR = ":";
    /** 合法 */
    public static final AtomicBoolean VALID = new AtomicBoolean(true);
    /** 不合法 */
    public static final AtomicBoolean INVALID = new AtomicBoolean(false);
    /** token */
    public static final String TOKEN = "token";
    /** sessionId */
    public static final String SESSION_ID = "sessionId";
    /** sso鉴权路径 */
    public static final String AUTHORIZATION_URL = "/sso/authorize";
}
