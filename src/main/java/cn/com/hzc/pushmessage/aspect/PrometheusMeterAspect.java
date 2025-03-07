package cn.com.hzc.pushmessage.aspect;


import cn.com.hzc.pushmessage.prom.MessagePrometheus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 统计数据切面
 */
@Slf4j
@Aspect
@Component
public class PrometheusMeterAspect {
    @Autowired
    private MessagePrometheus messagePrometheus;

    /**
     * redis消息监听
     */
    @Pointcut("execution(public * cn.com.gffunds.pushmessage.listener.*.onMessage(..))")
    public void redisOnMessage() {
        // 监听切面
        }

    /**
     * 客户端订阅
     */
    @Pointcut("execution(public * cn.com.gffunds.pushmessage..MessageConsumer.subscribe(..))")
    public void subscribe() {
        // 订阅切面
    }

    /**
     * 客户端退订
     */
    @Pointcut("execution(public * cn.com.gffunds.pushmessage..MessageConsumer.unsubscribe(..))")
    public void unsubscribe() {
        // 退订切面
    }

    /**
     * redis监听信息统计
     */
    @Around("redisOnMessage()")
    public Object redisOnMessageAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            String methodName = proceedingJoinPoint.getSignature().getName();
            // 统计redis推送
            messagePrometheus.importantMethodCount("redis推送", methodName);
        }
    }

    /**
     * 客户端订阅监听
     */
    @Around("subscribe()")
    public Object subscribeAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            String methodName = proceedingJoinPoint.getSignature().getName();
            // 统计客户端订阅
            messagePrometheus.importantMethodCount("客户端订阅", methodName);
        }
    }

    /**
     * 客户端退订监听
     */
    @Around("unsubscribe()")
    public Object unsubscribeAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            String methodName = proceedingJoinPoint.getSignature().getName();
            // 统计客户端退订
            messagePrometheus.importantMethodCount("客户端退订", methodName);
        }
    }
}