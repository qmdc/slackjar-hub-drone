package com.slack.slackjarservice.foundation.socketio;

import cn.dev33.satoken.stp.StpUtil;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.slack.slackjarservice.common.enumtype.foundation.PushWithFrontEnum;
import com.slack.slackjarservice.foundation.model.dto.SocketMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * SocketIO事件处理器 前端推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FrontMessagePushEvent {

    private final BackendMessagePush socketMessagePush;

    /**
     * 监听前端发送的消息事件
     *
     * @param client SocketIO客户端连接
     * @param data 客户端发送的数据
     */
    @OnEvent("front-message")
    public void onClientMessage(SocketIOClient client, SocketMessageDTO data) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        String platformId = client.getHandshakeData().getSingleUrlParam("platformId");
        if (Objects.isNull(userId) || Objects.isNull(platformId)) {
            log.warn("无效的连接参数，userId:{},platformId:{},sessionId:{}", userId, platformId, client.getSessionId());
            return;
        }
        if (!StpUtil.isLogin(userId)) {
            log.warn("用户未登录，拒绝消息，userId:{}", userId);
            return;
        }
        PushWithFrontEnum.verifyBizType(data.getBizType());

        log.info("收到来自用户[{}]的[{}]端消息: {}", userId, platformId, data.toString());
    }
}
