package cn.com.gffunds.pushmessage.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author hezhc
 * @date 2023/2/24 17:38
 * @description irmRedis配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "spring.redis.irm")
@Configuration
@RefreshScope
public class IrmRedisProperties extends DefaultRedisProperties{
}
