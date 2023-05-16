package cn.com.gffunds.pushmessage.config;

import cn.com.gffunds.pushmessage.listener.RedisMessageListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class MyImportBean implements BeanFactoryAware {

    @Autowired
    private SourceProperties sourceProperties;

    @Autowired
    private RedisConfig redisConfig;

    @Autowired
    private SubscribeConfig subscribeConfig;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        for (SourceProperties.RedisProperties redisProperty : sourceProperties.getRedis()) {
            String id = redisProperty.getId();
            String factory = id + "RedisConnectionFactory";
            String redisTemplate = id + "RedisTemplate";
            LettuceConnectionFactory lettuceConnectionFactory = redisConfig.defaultRedisConnectionFactory(redisProperty);
            listableBeanFactory.registerSingleton(factory, lettuceConnectionFactory);
            listableBeanFactory.registerSingleton(redisTemplate, redisConfig.defaultRedisTemplate(lettuceConnectionFactory));
            RedisMessageListener redisMessageListener = new RedisMessageListener(id);
            listableBeanFactory.registerSingleton(id + "RedisMessageListener", redisMessageListener);
            listableBeanFactory.registerSingleton(id + "RedisMessageListenerContainer", subscribeConfig.generateRedisMessageListenerContainer(redisMessageListener, lettuceConnectionFactory, redisProperty));
            BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(LettuceConnectionFactory.class).getBeanDefinition();
            beanDefinition.setPrimary(true);
            listableBeanFactory.registerBeanDefinition(factory, beanDefinition);
        }
    }
//
//    @Override
//    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
////        for (SourceProperties.RedisProperties redisProperty : sourceProperties.getRedis()) {
////            LettuceConnectionFactory lettuceConnectionFactory = redisConfig.defaultRedisConnectionFactory(redisProperty);
////        }
//    }
//
//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//
//    }
}