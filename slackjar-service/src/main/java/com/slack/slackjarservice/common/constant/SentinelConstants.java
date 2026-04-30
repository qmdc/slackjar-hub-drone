package com.slack.slackjarservice.common.constant;

/**
 * Sentinel 熔断策略常量
 * 对应 Sentinel 的熔断策略类型
 *
 * @author zhn
 */
public interface SentinelConstants {

    /**
     * 熔断策略类型
     */
    interface CircuitBreakerStrategy {
        /**
         * 慢调用比例策略
         */
        int SLOW_REQUEST_RATIO = 0;
        /**
         * 异常比例策略
         */
        int ERROR_RATIO = 1;
        /**
         * 异常数策略
         */
        int ERROR_COUNT = 2;
    }
}
