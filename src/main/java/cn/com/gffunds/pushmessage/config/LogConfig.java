package cn.com.gffunds.pushmessage.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author hezhc
 * @date 2023/3/2 11:13
 * @description
 */
@Configuration
@RefreshScope
@Data
public class LogConfig {
    @Value("${log.enable}")
    private boolean logEnable;
}
