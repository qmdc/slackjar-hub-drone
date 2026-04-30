package com.slack.slackjarservice.foundation.listener;

import com.slack.slackjarservice.foundation.event.OssRefreshEvent;
import com.slack.slackjarservice.foundation.service.SysFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * OSS配置更新事件监听器
 * 监听OSS存储配置更新事件，并刷新文件存储策略
 *
 * @author zhn
 * @since 2025-08-26
 */
@Slf4j
@Component
public class OssRefreshListener {

    @Resource
    private SysFileService sysFileService;

    /**
     * 监听OSS配置更新事件
     * @param event OSS配置更新事件
     */
    @EventListener
    public void handleOssConfigUpdate(OssRefreshEvent event) {
        log.info("收到OSS配置更新事件");
        sysFileService.refreshActivePolicy();
    }

}
