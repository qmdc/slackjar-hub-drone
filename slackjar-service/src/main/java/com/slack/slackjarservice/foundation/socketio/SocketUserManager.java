package com.slack.slackjarservice.foundation.socketio;

import cn.dev33.satoken.stp.StpUtil;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SocketIO用户连接管理服务
 *
 * @author zhn
 */
@Slf4j
@Service
public class SocketUserManager {

    /**
     * 用户连接信息存储
     * 结构：用户ID -> (登录端 -> 用户Session)
     */
    private static final Map<String, Map<String, SocketIOClient>> userClients = new ConcurrentHashMap<>();

    /**
     * 用户连接时触发
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        String platformId = client.getHandshakeData().getSingleUrlParam("platformId");

        if (Objects.nonNull(userId) && Objects.nonNull(platformId)) {
            if (!StpUtil.isLogin(userId)) {
                log.warn("用户未登录，拒绝连接，userId:{}", userId);
                return;
            }
            // 将用户连接信息存储到Map中
            userClients.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(platformId, client);
            log.info("已建立用户连接，userId:{},platformId:{}", userId, platformId);
        } else {
            log.warn("无效的连接参数，userId:{},platformId:{},sessionId:{}", userId, platformId, client.getSessionId());
            client.disconnect();
        }
    }

    /**
     * 用户断开连接时触发
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        // 获取用户ID和登录端信息
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        String platformId = client.getHandshakeData().getSingleUrlParam("platformId");

        if (Objects.nonNull(userId) && Objects.nonNull(platformId)) {
            // 从Map中移除用户连接信息
            Map<String, SocketIOClient> platformClients = userClients.get(userId);
            if (Objects.nonNull(platformClients)) {
                platformClients.remove(platformId);

                // 如果该用户没有其他端的连接，则移除整个用户条目
                if (platformClients.isEmpty()) {
                    userClients.remove(userId);
                }
            }

            log.info("已断开用户连接，userId:{},platformId:{}", userId, platformId);
        } else {
            log.warn("无效的断开连接参数，userId: {}, platform: {}", userId, platformId);
        }
    }

    /**
     * 获取指定用户的所有连接
     */
    public Map<String, SocketIOClient> getUserClients(String userId) {
        Map<String, SocketIOClient> userSocketIOClientMap = userClients.get(userId);
        if (userSocketIOClientMap == null || userSocketIOClientMap.isEmpty()) {
            userClients.remove(userId);
        }
        return userSocketIOClientMap;
    }

    /**
     * 获取所有用户连接信息
     */
    public Map<String, Map<String, SocketIOClient>> getAllUserClients() {
        return userClients;
    }
}
