package com.slack.slackjarservice.foundation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * OSS存储配置更新事件
 * 当系统配置中的文件存储策略发生变化时触发此事件
 *
 * @author zhn
 * @since 2025-08-26
 */
@Getter
public class OssRefreshEvent extends ApplicationEvent {

    public OssRefreshEvent(Object source) {
        super(source);
    }

}
