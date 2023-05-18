package cn.com.gffunds.pushmessage.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/24 17:38
 * @description irmRedis配置
 */
@Data
@ConfigurationProperties(prefix = "spring")
@Configuration
@RefreshScope
@Accessors(chain = true)
public class SourceProperties {
    private List<RedisProperties> redis;


    @Data
    @RefreshScope
    @EqualsAndHashCode(callSuper = true)
    public static class RedisProperties extends DefaultRedisProperties {
        private String id;
    }
}
