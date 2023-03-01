package cn.com.gffunds.pushmessage.util;

import cn.com.gffunds.commons.concurrent.ThreadPoolUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
@Component
public class SendMessageThreadPool implements ApplicationListener<ContextStoppedEvent> {

    /**
     * 发送消息线程池
     */
    private static final ExecutorService POOL = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() + 1,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(128),
            new NamedThreadFactory("send-message-thread", true),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return ContextualCompletableFuture.supply(supplier, POOL);
    }

    public static <T> CompletableFuture<T> supplyNoException(Supplier<T> supplier) {
        return ContextualCompletableFuture.supplyNoException(supplier, POOL);
    }

    public static void runAsync(Runnable runnable) {
        ContextualCompletableFuture.runAsync(runnable, POOL);
    }

    public static void runAsyncNoException(Runnable runnable) {
        ContextualCompletableFuture.runAsyncNoException(runnable, POOL);
    }

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        log.info("关闭SendMessageThreadPool 线程池");
        ThreadPoolUtil.gracefulShutdown(POOL, 30, TimeUnit.SECONDS);
    }

}
