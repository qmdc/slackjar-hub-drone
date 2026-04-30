package com.slack.slackjarservice.common.config;

import com.slack.slackjarservice.common.util.RandomUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TraceId 拦截器
 * 为每个请求生成唯一的 TraceId，用于日志链路追踪
 *
 * @author zhn
 */
@Slf4j
@Component
public class TraceInterceptor implements HandlerInterceptor {

    /**
     * TraceId 请求头名称
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * MDC 中的 TraceId 键名
     */
    public static final String TRACE_ID_KEY = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先从请求头获取（支持链路传递）
        String traceId = request.getHeader(TRACE_ID_HEADER);

        // 如果请求头没有，则生成新的 TraceId
        if (traceId == null || traceId.isEmpty()) {
            traceId = RandomUtil.generateTraceId();
        }

        // 放入 MDC
        MDC.put(TRACE_ID_KEY, traceId);

        // 设置响应头，方便前端排查问题
        response.setHeader(TRACE_ID_HEADER, traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // 请求结束后清除 MDC，避免内存泄漏
        MDC.remove(TRACE_ID_KEY);
    }
}
