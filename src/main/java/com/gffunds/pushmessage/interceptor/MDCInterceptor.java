package com.gffunds.pushmessage.interceptor;

import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.gffunds.pushmessage.constants.MDCConstants.TRACE_ID;


public class MDCInterceptor extends HandlerInterceptorAdapter {

    /**
     * 设置traceId
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //在日志框架的MDC中设置traceId值
        MDC.put(TRACE_ID, uuid);
        //在响应添加Trace-Id头部
        response.setHeader("Trace-ID", uuid);
        return true;
    }

    /**
     * 清理traceId
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //完成请求后将traceId删除，否则每个新请求添加一个traceId会导致内存泄露
        MDC.remove(TRACE_ID);
    }
}
