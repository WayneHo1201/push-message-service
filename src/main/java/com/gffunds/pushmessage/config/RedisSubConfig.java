package com.gffunds.pushmessage.config;

import com.gffunds.pushmessage.listener.RedisMessageListener;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;

@ConfigurationProperties(prefix = "spring.redis")
@Configuration
@RefreshScope
@Data
public class RedisSubConfig {

    private List<String> subscribes;

    @Bean
    @Primary
    public RedisMessageListenerContainer container(RedisConnectionFactory factory, RedisMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        //订阅频道 这个container可以添加多个messageListener
        for (String subscribe : subscribes) {
            container.addMessageListener(listener, new ChannelTopic(subscribe));
        }
        return container;
    }
 
}