package cn.com.hzc.pushmessage.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.com.hzc.pushmessage.common.enumeration.ErrCodeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 返回结果包装类
 *
 * @author liyuan
 * 20210616
 */
@Data
@Accessors(chain = true)
public class ReturnResult<T> {
    @JsonProperty("errcode")
    private String errorCode;
    @JsonProperty("errmsg")
    private String errorMsg;
    @JsonProperty("data")
    private T data;


    public ReturnResult() {
        this.errorCode = String.valueOf(ErrCodeEnum.SUCCESS.code());
        this.errorMsg = ErrCodeEnum.SUCCESS.msg();
    }

    public ReturnResult(T data) {
        this.errorCode = String.valueOf(ErrCodeEnum.SUCCESS.code());
        this.errorMsg = ErrCodeEnum.SUCCESS.msg();
        this.data = data;
    }

    public ReturnResult(ErrCodeEnum commonCodeEnum) {
        this.errorCode = String.valueOf(commonCodeEnum.code());
        this.errorMsg = commonCodeEnum.msg();
    }

    public ReturnResult(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        // 兼容sso成功错误码
        return ErrCodeEnum.SUCCESS.code().equals(errorCode)|| "0".equals(errorCode);
    }

}