package cn.com.gffunds.pushmessage.exception;


import cn.com.gffunds.pushmessage.common.enumeration.ErrCodeEnum;
import lombok.*;

/**
 * @description: 自定义异常信息的异常类
 * @author: Wu Teng
 * @email: wut@gffunds.com.cn
 * @date: 2021/8/2 15:50
 * @modified By：
 * @version: 1.0.0$
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PushMessageException extends Exception{
    private String errorReason;
    private String errorCode;

    public PushMessageException(String errorReason) {
        this.errorReason = errorReason;
        this.errorCode = ErrCodeEnum.INTERNAL_SERVER_ERROR.code();
    }

}
