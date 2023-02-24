package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/24 17:38
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ConfigurationProperties(prefix = "spring.redis.ipm")
@Configuration
public class IpmRedisProperties extends DefaultRedisProperties{
    private List<BizTopic> subscribes;
}
