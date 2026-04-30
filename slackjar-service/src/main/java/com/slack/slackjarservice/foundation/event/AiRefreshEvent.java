package com.slack.slackjarservice.foundation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Ai模型策略刷新事件
 * 当系统配置中的Ai策略发生变化时触发此事件
 *
 * @author zhn
 * @since 2025-08-30
 */
@Getter
public class AiRefreshEvent extends ApplicationEvent {

    public AiRefreshEvent(Object source) {
        super(source);
    }

}
