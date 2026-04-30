package com.slack.slackjarservice.foundation.listener;

import com.slack.slackjarservice.foundation.event.AiRefreshEvent;
import com.slack.slackjarservice.foundation.service.AiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * AI模型策略刷新事件监听器
 * 监听AI配置更新事件，并刷新AI模型策略
 *
 * @author zhn
 * @since 2025-08-30
 */
@Slf4j
@Component
public class AiRefreshListener {

    @Resource
    private AiService aiService;

    /**
     * 监听AI配置更新事件
     * @param event AI配置更新事件
     */
    @EventListener
    public void handleOssConfigUpdate(AiRefreshEvent event) {
        log.info("收到AI配置更新事件，刷新AI模型策略");
        aiService.refreshActivePolicy();
    }

}
