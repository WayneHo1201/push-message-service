package cn.com.gffunds.pushmessage.exception;


/**
 * @description: 自定义异常信息的异常类
 * @author: Wu Teng
 * @email: wut@gffunds.com.cn
 * @date: 2021/8/2 15:50
 * @modified By：
 * @version: 1.0.0$
 */
public class PushMessageException extends Exception{
    private final String errorReason;

    public PushMessageException(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getMessage() {
        return this.errorReason;
    }

}
