package com.slack.slackjarservice.common.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.slack.slackjarservice.common.constant.SentinelConstants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 规则配置
 * 可通过 Sentinel 控制台动态调整规则，此处配置作为默认规则
 *
 * @author zhn
 */
@Slf4j
@Configuration
public class SentinelRuleConfig {

    @Value("${sentinel.enabled:true}")
    private boolean sentinelEnabled;

    /**
     * 初始化限流和熔断规则
     */
    @PostConstruct
    public void initRules() {
        if (!sentinelEnabled) {
            log.info("Sentinel 已禁用，跳过规则初始化");
            return;
        }

        initFlowRules();
        initDegradeRules();

        log.info("Sentinel 规则初始化完成");
    }

    /**
     * 初始化限流规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 对 AI 接口进行限流，每秒最多 5 个请求
        FlowRule aiFlowRule = new FlowRule();
        aiFlowRule.setResource("ai-chat");
        aiFlowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiFlowRule.setCount(5);
        aiFlowRule.setLimitApp("default");
        rules.add(aiFlowRule);

        // 对文件上传接口进行限流，每秒最多 5 个请求
        FlowRule fileUploadRule = new FlowRule();
        fileUploadRule.setResource("file-upload");
        fileUploadRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        fileUploadRule.setCount(5);
        fileUploadRule.setLimitApp("default");
        rules.add(fileUploadRule);

        FlowRuleManager.loadRules(rules);
        log.info("加载限流规则: {} 条", rules.size());
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // AI 服务熔断规则 - 异常比例模式
        // 当 1 秒内异常比例超过 50% 时触发熔断
        DegradeRule aiDegradeRule = new DegradeRule();
        aiDegradeRule.setResource("ai-chat");
        aiDegradeRule.setGrade(SentinelConstants.CircuitBreakerStrategy.ERROR_RATIO);
        aiDegradeRule.setCount(0.5);
        aiDegradeRule.setTimeWindow(10);
        aiDegradeRule.setMinRequestAmount(5);
        aiDegradeRule.setStatIntervalMs(1000);
        rules.add(aiDegradeRule);

        // 外部接口熔断规则 - 慢调用比例模式
        // 当 1 秒内慢调用比例超过 60% 时触发熔断
        DegradeRule slowCallRule = new DegradeRule();
        slowCallRule.setResource("third-party-api");
        slowCallRule.setGrade(SentinelConstants.CircuitBreakerStrategy.SLOW_REQUEST_RATIO);
        slowCallRule.setCount(500);
        slowCallRule.setSlowRatioThreshold(0.6);
        slowCallRule.setTimeWindow(15);
        slowCallRule.setMinRequestAmount(10);
        slowCallRule.setStatIntervalMs(1000);
        rules.add(slowCallRule);

        DegradeRuleManager.loadRules(rules);
        log.info("加载熔断规则: {} 条", rules.size());
    }
}
