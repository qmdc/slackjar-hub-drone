package com.slack.slackjarservice.foundation.service;

import com.slack.slackjarservice.foundation.model.dto.ServerConfigDTO;

/**
 * 服务器Shell语句执行服务层
 *
 * @author zhn
 * @since 2025-08-26
 */
public interface ShellService {

    /**
     * 执行Shell命令-主服务器
     *
     * @param command 命令
     * @param serverConfig 服务器配置
     * @return 执行结果
     */
    String executeCommand(String command, ServerConfigDTO serverConfig);

}
