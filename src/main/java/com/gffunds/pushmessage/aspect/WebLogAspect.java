package com.gffunds.pushmessage.aspect;

import cn.com.gffunds.commons.json.JacksonUtil;
import com.gffunds.pushmessage.common.ReturnResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author guxh
 * @date 2020/10/27
 * @description
 */
@Order(1)
@Aspect
@Component
@Slf4j
public class WebLogAspect {

    private static final int logMaxLen = 4000;

    /**
     * 以 controller 包下定义的所有请求为切入点
     */
    @Pointcut("execution(public * com.gffunds.pushmessage.controller..*.*(..)) " +
            "&& !execution(* com.gffunds.pushmessage.handler.GlobalExceptionHandler.*(..))")
    public void webLog() {
    }

    /**
     * 在切点之前织入
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        if (log.isInfoEnabled()) {
            // 开始打印请求日志
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            if (request.getRequestURL().indexOf("/database/encrypt") != -1 || request.getRequestURL().indexOf("/database/decrypt") != -1) {
                return;
            }
            //过滤掉存在死循环的对象
            Object[] objects = Arrays.stream(joinPoint.getArgs())
                    .filter(object -> !(object instanceof HttpServletResponse) && !(object instanceof MultipartFile)).toArray();

            //读取头部
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }

            // 打印请求 url、Http method、controller 的全路径以及执行方法、请求的IP、请求入参
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("URL", request.getRequestURL().toString());
            logMap.put("HTTP Method", request.getMethod());
            logMap.put("Class Method", String.format("%s.%s", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName()));
            logMap.put("IP", request.getRemoteAddr());
            logMap.put("Headers", headers);
            logMap.put("Request Args", objects);
            log.info("=======================================请求开始=======================================");
            log.info(JacksonUtil.toJson(logMap));
        }
    }

    /**
     * 在切点之后织入
     */
    @After("webLog()")
    public void doAfter() {
        if (log.isInfoEnabled()) {
            log.info("=======================================请求结束=======================================");
        }
    }

    /**
     * 环绕
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        if (log.isInfoEnabled()) {
            // 打印出参、执行耗时
            Object response = null;
            if (result instanceof ReturnResult) {
                response = result;
            }
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("Time-Consuming", String.format("%sms", System.currentTimeMillis() - startTime));
            logMap.put("Response", response);
            String json = JacksonUtil.toJson(logMap);
            log.info(json.length() > logMaxLen ? json.substring(0, logMaxLen).concat("【已截断】") : json);
        }
        return result;
    }
}