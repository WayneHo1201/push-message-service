package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.exception.PushMessageException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 控制配置类的加载顺序,先加载 RedisAutoConfiguration.class 再加载该类,这样才能覆盖默认的 RedisTemplate
 *
 * @author guxh
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisConfig {
    @Autowired
    private IrmRedisProperties irmRedisProperties;

    @Autowired
    private IpmRedisProperties ipmRedisProperties;

    @Bean("irmRedisTemplate")
    public RedisTemplate<String, Object> irmRedisTemplate() {
        return defaultRedisTemplate(irmRedisConnectionFactory());
    }

    @Bean("irmRedisConnectionFactory")
    @Primary
    public LettuceConnectionFactory irmRedisConnectionFactory() {
        return defaultRedisConnectionFactory(irmRedisProperties);
    }

    @Bean("ipmRedisTemplate")
    public RedisTemplate<String, Object> ipmRedisTemplate() {
        return defaultRedisTemplate(ipmRedisConnectionFactory());
    }

    @Bean("ipmRedisConnectionFactory")
    public LettuceConnectionFactory ipmRedisConnectionFactory() {
        return defaultRedisConnectionFactory(ipmRedisProperties);
    }

    /**
     * 单例模式的redisConnectionFactory
     */
    private LettuceConnectionFactory defaultRedisConnectionFactory(DefaultRedisProperties redisProperties) {
        if (redisProperties.getSentinel() != null) {
            RedisConfiguration configuration = defaultSentinelRedisConnectionFactory(redisProperties.getSentinel());
            return getLettuceConnectionFactory(redisProperties, configuration);
        } else {
            // 基本配置
            RedisStandaloneConfiguration configuration = getRedisStandaloneConfiguration(redisProperties);
            return getLettuceConnectionFactory(redisProperties, configuration);
        }
    }

    /**
     * 单例模式的redisConnectionFactory
     */
    private RedisStandaloneConfiguration getRedisStandaloneConfiguration(DefaultRedisProperties redisProperties) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());
        if (Strings.isNotBlank(redisProperties.getPassword())) {
            configuration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        return configuration;
    }


    /**
     * sentinel模式的redisConnectionFactory
     */
    private RedisSentinelConfiguration defaultSentinelRedisConnectionFactory(DefaultRedisProperties.Sentinel sentinel) {
        // 基本配置
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        configuration.setMaster(sentinel.getMaster());
        if (Strings.isNotBlank(sentinel.getPassword())) {
            configuration.setPassword(RedisPassword.of(sentinel.getPassword()));
        }
        List<RedisNode> redisNodeList = createSentinels(sentinel);
        configuration.setSentinels(redisNodeList);
        return configuration;
    }

    private LettuceConnectionFactory getLettuceConnectionFactory(DefaultRedisProperties redisProperties, RedisConfiguration configuration) {
        // 连接池配置
        GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(redisProperties.getMaxActive());
        genericObjectPoolConfig.setMaxWaitMillis(redisProperties.getMaxWait());
        genericObjectPoolConfig.setMaxIdle(redisProperties.getMaxIdle());
        genericObjectPoolConfig.setMinIdle(redisProperties.getMinIdle());
        // lettuce pool
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(genericObjectPoolConfig);
        builder.commandTimeout(Duration.ofSeconds(redisProperties.getTimeout()));
        return new LettuceConnectionFactory(configuration, builder.build());
    }


    /**
     * 自定义 redisTemplate （方法名一定要叫 redisTemplate 因为 @Bean 是根据方法名配置这个bean的name的）
     * 默认的 RedisTemplate<K,V> 为泛型，使用时不太方便，自定义为 <String, Object>
     * 默认序列化方式为 JdkSerializationRedisSerializer 序列化后的内容不方便阅读，改为序列化成 json
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return defaultRedisTemplate(redisConnectionFactory);
    }

    public RedisTemplate<String, Object> defaultRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 配置 json 序列化器 - Jackson2JsonRedisSerializer
        Jackson2JsonRedisSerializer jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonSerializer.setObjectMapper(objectMapper);

        // 创建并配置自定义 RedisTemplateRedisOperator
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 将 key 序列化成字符串
        template.setKeySerializer(new StringRedisSerializer());
        // 将 hash 的 key 序列化成字符串
        template.setHashKeySerializer(new StringRedisSerializer());
        // 将 value 序列化成 json
        template.setValueSerializer(jacksonSerializer);
        // 将 hash 的 value 序列化成 json
        template.setHashValueSerializer(jacksonSerializer);
        template.afterPropertiesSet();
        return template;
    }


    @SneakyThrows
    private List<RedisNode> createSentinels(DefaultRedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : sentinel.getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(parts != null && parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel property '" + node + "'");
            }
        }
        return nodes;
    }

}

