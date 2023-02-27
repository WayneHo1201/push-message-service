package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.exception.PushMessageException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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
        return redisTemplate(irmRedisConnectionFactory());
    }

    @Bean("irmRedisConnectionFactory")
    @Primary
    public LettuceConnectionFactory irmRedisConnectionFactory() {
       return defaultRedisConnectionFactory(irmRedisProperties);
    }

    @Bean("ipmRedisTemplate")
    public RedisTemplate<String, Object> ipmRedisTemplate() {
        return redisTemplate(ipmRedisConnectionFactory());
    }

    @Bean("ipmRedisConnectionFactory")
    public LettuceConnectionFactory ipmRedisConnectionFactory() {
        return defaultSentinelRedisConnectionFactory(ipmRedisProperties);
    }

    /**
     * 单例模式的redisConnectionFactory
     */
    private LettuceConnectionFactory defaultRedisConnectionFactory(DefaultRedisProperties redisProperties) {
        // 基本配置
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());
        if (Strings.isNotBlank(redisProperties.getPassword())) {
            configuration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
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
     * sentinel模式的redisConnectionFactory
     */
    private LettuceConnectionFactory defaultSentinelRedisConnectionFactory(DefaultSentinelRedisProperties redisProperties) {
        // 基本配置
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        configuration.setMaster(redisProperties.getMaster());
        if (Strings.isNotBlank(redisProperties.getPassword())) {
            configuration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        List<RedisNode> redisNodeList = getRedisNodes(redisProperties.getNodes());
        configuration.setSentinels(redisNodeList);
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


    /**
     * 解析sentinel配置
     */
    @SneakyThrows
    private List<RedisNode> getRedisNodes(String nodes) {
        List<RedisNode> redisNodeList = new ArrayList<>();
        try {
            if (StringUtils.isNotBlank(nodes)) {
                String[] paths = nodes.split(",");
                for (String path : paths) {
                    String[] split = path.split(":");
                    redisNodeList.add(new RedisNode(split[0], Integer.parseInt(split[1])));
                }
            }
        } catch (Exception e) {
            throw new PushMessageException("redis sentinel配置异常，请检查nacos配置！");
        }
        return redisNodeList;
    }

}

