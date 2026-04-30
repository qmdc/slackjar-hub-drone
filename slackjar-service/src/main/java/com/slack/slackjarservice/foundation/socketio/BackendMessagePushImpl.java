package com.slack.slackjarservice.foundation.socketio;

import com.corundumstudio.socketio.SocketIOClient;
import com.slack.slackjarservice.common.constant.CommonConstants;
import com.slack.slackjarservice.foundation.model.dto.SocketMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Socket消息推送服务实现类 后端推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackendMessagePushImpl implements BackendMessagePush {

    private final SocketUserManager socketIOUserService;

    /**
     * 推送消息给批量用户的所有登录端
     */
    @Override
    public void pushMessageToUsers(List<String> userIds, SocketMessageDTO message) {
        for (String userId : userIds) {
            pushMessageToUser(userId, message);
        }
    }

    /**
     * 推送消息给指定用户的所有登录端
     */
    @Override
    public void pushMessageToUser(String userId, SocketMessageDTO message) {
        // 获取用户的所有连接
        Map<String, SocketIOClient> userClients = socketIOUserService.getUserClients(userId);

        if (userClients != null && !userClients.isEmpty()) {
            // 向用户的所有登录端推送消息
            userClients.forEach((platform, client) -> {
                if (client.isChannelOpen()) {
                    client.sendEvent(CommonConstants.SocketIoPushEventType.BACKEND_MESSAGE, message);
//                    log.info("向用户 {} 的 {} 端推送消息: {}", userId, platform, message);
                } else {
                    // 如果连接已关闭，则从用户连接信息中移除
                    userClients.remove(platform);
                    log.warn("用户 {} 的 {} 端连接已关闭，无法推送消息", userId, platform);
                }
            });
        } else {
            log.warn("用户 {} 没有活跃的连接，无法推送消息", userId);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    @Override
    public void broadcastMessage(SocketMessageDTO message) {
        // 获取所有用户的连接
        Map<String, Map<String, SocketIOClient>> allUserClients = socketIOUserService.getAllUserClients();
        int totalUsers = 0;
        int totalConnections = 0;
        int successCount = 0;
        int failCount = 0;

        if (allUserClients != null && !allUserClients.isEmpty()) {
            // 遍历所有用户的所有连接
            for (Map.Entry<String, Map<String, SocketIOClient>> userEntry : allUserClients.entrySet()) {
                String userId = userEntry.getKey();
                Map<String, SocketIOClient> userClients = userEntry.getValue();
                totalUsers++;
                totalConnections += userClients.size();

                // 向用户的所有登录端推送消息
                for (Map.Entry<String, SocketIOClient> clientEntry : userClients.entrySet()) {
                    String platform = clientEntry.getKey();
                    SocketIOClient client = clientEntry.getValue();

                    if (client.isChannelOpen()) {
                        client.sendEvent(CommonConstants.SocketIoPushEventType.BACKEND_MESSAGE, message);
                        log.info("广播消息给用户 {} 的 {} 端: {}", userId, platform, message);
                        successCount++;
                    } else {
                        // 如果连接已关闭，则从用户连接信息中移除
                        userClients.remove(platform);
                        log.warn("用户 {} 的 {} 端连接已关闭，无法广播消息", userId, platform);
                        failCount++;
                    }
                }
            }
        }
        log.info("广播消息完成:总用户数={},总连接数={},成功数={},失败数={}", totalUsers, totalConnections, successCount, failCount);
    }
}
