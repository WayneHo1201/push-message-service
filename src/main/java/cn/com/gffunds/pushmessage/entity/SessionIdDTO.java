package cn.com.gffunds.pushmessage.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author hezhc
 * @date 2023/2/24 10:33
 * @description 鉴权
 */
@Data
@Accessors(chain = true)
public class SessionIdDTO {
    @NotBlank(message = "sessionId不能为空")
    private String sessionId;
}
