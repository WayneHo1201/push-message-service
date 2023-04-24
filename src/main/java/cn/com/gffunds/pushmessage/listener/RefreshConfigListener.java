package cn.com.gffunds.pushmessage.listener;

import cn.com.gffunds.pushmessage.service.RefreshService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

/**
 * @author hezhc
 * @date 2023/4/14 16:47
 * @description nacos配置刷新监听器
 */
@Configuration
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RefreshConfigListener implements ApplicationListener<ApplicationEvent> {

    public static final String REFRESHED_EVENT_CLASS = "org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent";

    @Autowired
    private RefreshService refreshService;

    /**
     * 监听事件
     */
    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationEvent event) {
        if (isAssignable(REFRESHED_EVENT_CLASS, event)) {
            log.info("监听到nacos配置变更，执行订阅配置刷新！");
            refreshService.refresh();
        }
    }

    /**
     * 判断是否为刷新类
     */
    boolean isAssignable(String className, Object value) {
        try {
            return ClassUtils.isAssignableValue(ClassUtils.forName(className, null), value);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

