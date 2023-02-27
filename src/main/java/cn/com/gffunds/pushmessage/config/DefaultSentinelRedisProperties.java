package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import lombok.Data;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/27 11:03
 * @description
 */
@Data
public class DefaultSentinelRedisProperties {
    private String master;
    private String nodes;
    private String password;
    private Integer maxActive;
    private Integer maxWait;
    private Integer maxIdle;
    private Integer minIdle;
    private Long timeout;
    private List<BizTopic> subscribes;
}
