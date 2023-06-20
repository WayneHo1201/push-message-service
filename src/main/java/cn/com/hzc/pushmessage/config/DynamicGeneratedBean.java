package cn.com.hzc.pushmessage.config;

import cn.com.hzc.pushmessage.listener.RedisMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 动态加载redis数据源
 */
@Component
@Slf4j
public class DynamicGeneratedBean implements BeanFactoryAware {

    @Autowired
    private SourceProperties sourceProperties;

    @Autowired
    private RedisConfig redisConfig;

    @Autowired
    private SubscribeConfig subscribeConfig;

    private static final String FACTORY = "RedisConnectionFactory";
    private static final String REDIS_TEMPLATE = "RedisTemplate";
    private static final String LISTENER = "RedisMessageListener";
    private static final String CONTAINER = "RedisMessageListenerContainer";

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        for (SourceProperties.RedisProperties redisProperty : sourceProperties.getRedis()) {
            String id = redisProperty.getId();
            LettuceConnectionFactory lettuceConnectionFactory = redisConfig.defaultRedisConnectionFactory(redisProperty);
            lettuceConnectionFactory.afterPropertiesSet();
            // register factory
            listableBeanFactory.registerSingleton(id + FACTORY, lettuceConnectionFactory);
            // register redisTemplate
            listableBeanFactory.registerSingleton(id + REDIS_TEMPLATE, redisConfig.defaultRedisTemplate(lettuceConnectionFactory));
            RedisMessageListener redisMessageListener = new RedisMessageListener(id);
            // register MessageListener
            listableBeanFactory.registerSingleton(id + LISTENER, redisMessageListener);
            RedisMessageListenerContainer container = subscribeConfig.generateRedisMessageListenerContainer(redisMessageListener, lettuceConnectionFactory, redisProperty);
            // register ListenerContainer
            listableBeanFactory.registerSingleton(id + CONTAINER, container);
            log.info("加载[{}]redis配置到IOC容器!", id);
        }
    }
}