package com.slack.slackjarservice.common.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

/**
 * TraceId 任务装饰器
 * 用于线程池中传递 TraceId，确保异步任务也能追踪到原始请求
 *
 * @author zhn
 */
public class TraceTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // 获取当前线程的 TraceId
        String traceId = MDC.get(TraceInterceptor.TRACE_ID_KEY);

        // 返回包装后的 Runnable，在执行时设置 TraceId
        return () -> {
            try {
                // 在子线程中设置 TraceId
                if (traceId != null) {
                    MDC.put(TraceInterceptor.TRACE_ID_KEY, traceId);
                }
                runnable.run();
            } finally {
                // 执行完毕后清除
                MDC.remove(TraceInterceptor.TRACE_ID_KEY);
            }
        };
    }
}
