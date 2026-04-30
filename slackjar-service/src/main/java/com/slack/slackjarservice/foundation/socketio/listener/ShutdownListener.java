package com.slack.slackjarservice.foundation.socketio.listener;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * SocketIO服务停止监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private final SocketIOServer socketIOServer;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        socketIOServer.stop();
        log.info("SocketIO服务已停止");
    }
}
