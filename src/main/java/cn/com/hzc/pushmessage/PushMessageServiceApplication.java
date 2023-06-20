package cn.com.hzc.pushmessage;

import cn.hutool.extra.spring.EnableSpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = { RedisReactiveAutoConfiguration.class, RedisAutoConfiguration.class})
@Slf4j
@EnableDiscoveryClient
@EnableSpringUtil
public class PushMessageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PushMessageServiceApplication.class, args);
    }
}
