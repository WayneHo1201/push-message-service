package cn.com.gffunds.pushmessage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author hezhc
 * @date 2023/2/24 17:38
 * @description
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
}
