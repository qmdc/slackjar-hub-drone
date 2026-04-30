package com.slack.slackjarservice.common.base;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

/**
 * 定时任务基类
 *
 * @author zhn
 */
@Slf4j
public abstract class BaseTask {

    @Resource(name = "operateLogExecutor")
    private Executor operateLogExecutor;

    /**
     * 系统操作日志记录
     */
    public void operateLogRecord(String content, String taskCode) {
        operateLogExecutor.execute(() -> log.info("{} {}", taskCode, content));
    }

}
