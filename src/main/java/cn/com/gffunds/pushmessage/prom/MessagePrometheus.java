package cn.com.gffunds.pushmessage.prom;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author hezhc
 * @date 2023/2/20 16:28
 * @description 推送消息监控
 */
@Slf4j
@Component
public class MessagePrometheus {
    @Resource
    private PrometheusMeterRegistry meterRegistry;

    /**
     *重要方法调用次数统计
     */
    public void importantMethodCount(String name, String method) {
        meterRegistry.counter(name,"method", method, "state", "count").increment();
    }

}
