package cn.com.hzc.pushmessage.util;

import cn.com.hzc.pushmessage.constants.MDCConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

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
    private ContextualCompletableFuture(){}

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier, ExecutorService executorService) {
        String traceId = MDC.get(MDCConstants.TRACE_ID);
        return CompletableFuture.supplyAsync(() -> {
            MDC.put(MDCConstants.TRACE_ID, traceId);
            T t = supplier.get();
            MDC.remove(MDCConstants.TRACE_ID);
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
        String traceId = MDC.get(MDCConstants.TRACE_ID);
        return CompletableFuture.runAsync(() -> {
            MDC.put(MDCConstants.TRACE_ID, traceId);
            runnable.run();
            MDC.remove(MDCConstants.TRACE_ID);
        }, executorService);
    }
}
