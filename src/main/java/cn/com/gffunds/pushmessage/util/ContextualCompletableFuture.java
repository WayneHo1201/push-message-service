package cn.com.gffunds.pushmessage.util;

import cn.com.gffunds.pushmessage.constants.MDCConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author ：guxh
 * @date ：Created in 2022/6/28 20:05
 * @description：
 * @modified By：
 */
@Slf4j
public class ContextualCompletableFuture {
    public static <T> CompletableFuture<T> supply(Supplier<T> supplier, ExecutorService executorService) {
        RequestAttributes originRequestAttributes = RequestContextHolder.getRequestAttributes();
        String traceId = MDC.get(MDCConstants.TRACE_ID);
        return CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(originRequestAttributes);
            MDC.put(MDCConstants.TRACE_ID, traceId);
            T t = supplier.get();
            MDC.remove(MDCConstants.TRACE_ID);
            RequestContextHolder.resetRequestAttributes();
            return t;
        }, executorService);
    }

    public static <T> CompletableFuture<T> supplyNoException(Supplier<T> supplier, ExecutorService executorService) {
        return supply(supplier, executorService).exceptionally(t -> {
            log.error("子线程任务发生异常", t);
            return null;
        });
    }

    public static CompletableFuture<Void> runAsyncNoException(Runnable runnable, ExecutorService executorService) {
        return runAsync(runnable, executorService).exceptionally(t -> {
            log.error("子线程任务发生异常", t);
            return null;
        });
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, ExecutorService executorService) {
        RequestAttributes originRequestAttributes = RequestContextHolder.getRequestAttributes();
        String traceId = MDC.get(MDCConstants.TRACE_ID);
        return CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(originRequestAttributes);
            MDC.put(MDCConstants.TRACE_ID, traceId);
            runnable.run();
            MDC.remove(MDCConstants.TRACE_ID);
            RequestContextHolder.resetRequestAttributes();
        }, executorService);
    }
}
