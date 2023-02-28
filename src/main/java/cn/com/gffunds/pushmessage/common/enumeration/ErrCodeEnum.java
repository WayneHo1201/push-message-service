package cn.com.gffunds.pushmessage.common.enumeration;

import java.util.Objects;

/**
 * @description: 错误码枚举类
 * @author: Wu Teng
 * @email: wut@gffunds.com.cn
 * @date: 2022/3/11 8:39
 * @modified By：
 * @version: 1.0.0$
 */
public enum ErrCodeEnum {
    /**
     * 成功
     */
    SUCCESS("00000", "SUCCESS"),
    /**
     * 系统内部错误
     */
    INTERNAL_SERVER_ERROR("E0001", "INTERNAL_SERVER_ERROR"),
    /**
     * 请求参数异常
     */
    REQUEST_ABNORMAL("U0001", "REQUEST_ABNORMAL"),
    /**
     * 参数校验错误
     */
    PARAM_VALIDATED_ERROR("U0002", "PARAM_VALIDATED_ERROR"),
    /**
     * 业务逻辑异常
     */
    REST_EXCEPTION("B0001", "REST_EXCEPTION"),

    /**
     * 鉴权异常
     */
    TOKEN_INCORRECT("U0101", "TOKEN_INCORRECT"),
    ;

    private String code;
    private String msg;

    ErrCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String msg() {
        return msg;
    }

    public String code() {
        return code;
    }

    public static ErrCodeEnum getEnum(String code) {
        for (ErrCodeEnum errcode : ErrCodeEnum.values()) {
            if (Objects.equals(errcode.code, code)) {
                return errcode;
            }
        }
        return null;
    }
}
