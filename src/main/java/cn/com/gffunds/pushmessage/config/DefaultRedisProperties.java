package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import lombok.Data;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/24 17:38
 * @description redis连接默认配置
 */
@Data
public class DefaultRedisProperties {
    private String host;
    private Integer port;
    private String password;
    private Integer maxActive;
    private Integer maxWait;
    private Integer maxIdle;
    private Integer minIdle;
    private Long timeout;
    private List<BizTopic> subscribes;

}
