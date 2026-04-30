package com.slack.slackjarservice.common.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 限流熔断配置
 *
 * @author zhn
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 注册 Sentinel 注解切面，支持 @SentinelResource 注解
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 全局限流/熔断异常处理（用于 @SentinelResource 注解的 blockHandler）
     * 示例：@SentinelResource(value = "test", blockHandler = "handleBlock", blockHandlerClass = SentinelConfig.class)
     */
    public static ApiResponse<String> handleBlock(BlockException e) {
        log.warn("Sentinel 限流/熔断触发: {}", e.getClass().getSimpleName());

        if (e instanceof FlowException) {
            return ApiResponse.error(ResponseEnum.RATE_LIMIT);
        } else if (e instanceof DegradeException) {
            return ApiResponse.error(ResponseEnum.SERVICE_DEGRADE);
        } else {
            return ApiResponse.error(ResponseEnum.RATE_LIMIT);
        }
    }

    /**
     * 全限流/熔断异常处理（带原参数）
     * 示例：@SentinelResource(value = "test", blockHandler = "handleBlockWithParam", blockHandlerClass = SentinelConfig.class)
     */
    public static ApiResponse<String> handleBlockWithParam(String param, BlockException e) {
        log.warn("Sentinel 限流/熔断触发, param: {}, exception: {}", param, e.getClass().getSimpleName());
        return handleBlock(e);
    }

}
