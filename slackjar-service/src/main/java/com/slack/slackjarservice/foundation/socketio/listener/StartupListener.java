package com.slack.slackjarservice.foundation.socketio.listener;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * SocketIO服务启动和停止监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private final SocketIOServer socketIOServer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 启动SocketIO服务
        socketIOServer.start();
        log.info("SocketIO服务已启动，监听端口: {}", socketIOServer.getConfiguration().getPort());
    }
}
