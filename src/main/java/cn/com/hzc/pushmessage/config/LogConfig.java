package cn.com.hzc.pushmessage.config;

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
    @Value("${log.enable:false}")
    private boolean logEnable;
}
