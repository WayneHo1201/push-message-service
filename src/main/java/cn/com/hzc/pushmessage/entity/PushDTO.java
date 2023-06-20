package cn.com.hzc.pushmessage.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushDTO {

    // 业务
    private String bizId;

    // 主题
    private String topic;

    // 数据
    private Object data;
}
