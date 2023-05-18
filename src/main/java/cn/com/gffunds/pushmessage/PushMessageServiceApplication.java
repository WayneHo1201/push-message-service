package cn.com.gffunds.pushmessage;

import cn.com.gffunds.gfsecurity.autoconfigure.config.SSOAutoConfiguration;
import cn.hutool.extra.spring.EnableSpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {SSOAutoConfiguration.class, RedisReactiveAutoConfiguration.class, RedisReactiveHealthContributorAutoConfiguration.class})
@Slf4j
@EnableDiscoveryClient
@EnableSpringUtil
public class PushMessageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PushMessageServiceApplication.class, args);
    }
}
