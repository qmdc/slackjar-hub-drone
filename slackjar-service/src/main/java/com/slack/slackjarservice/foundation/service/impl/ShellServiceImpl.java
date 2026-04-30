package com.slack.slackjarservice.foundation.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.foundation.model.dto.ServerConfigDTO;
import com.slack.slackjarservice.foundation.service.ShellService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 服务器Shell语句执行实现类
 *
 * @author zhn
 * @since 2025-08-26
 */
@Slf4j
@Service
public class ShellServiceImpl implements ShellService {

    @Override
    public String executeCommand(String command, ServerConfigDTO serverConfig) {

        Session session = null;
        ChannelExec channel = null;
        StringBuilder result = new StringBuilder();

        try {
            // 建立SSH会话
            JSch jsch = new JSch();
            session = jsch.getSession(serverConfig.getServerUsername(), serverConfig.getServerIp(), serverConfig.getServerPort());
            session.setPassword(serverConfig.getServerPassword());

            // 跳过主机密钥校验（生产环境建议配置密钥）
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(3000);

            // 打开执行命令的通道
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            // 使用try-with-resources管理BufferedReader资源
            try (BufferedReader br = new BufferedReader(new InputStreamReader(channel.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            log.error("执行Shell命令异常：{}", e.getMessage(), e);
            throw new BusinessException(ResponseEnum.SHELL_COMMAND_ERROR);
        } finally {
            // 关闭资源
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return result.toString().trim();
    }

}
