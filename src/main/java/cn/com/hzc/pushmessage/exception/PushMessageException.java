package cn.com.hzc.pushmessage.exception;


import cn.com.hzc.pushmessage.common.enumeration.ErrCodeEnum;
import lombok.*;

/**
 * @description: 自定义异常信息的异常类
 * @author: Wu Teng
 * @email: wut@gffunds.com.cn
 * @date: 2021/8/2 15:50
 * @modified By：
 * @version: 1.0.0$
 */
@Getter
public class PushMessageException extends RuntimeException {
    private final String errorCode;

    public PushMessageException(String errorReason) {
        super(errorReason);
        this.errorCode = ErrCodeEnum.INTERNAL_SERVER_ERROR.code();
    }

    public PushMessageException(String errorReason, String code) {
        super(errorReason);
        this.errorCode = code;
    }
}
