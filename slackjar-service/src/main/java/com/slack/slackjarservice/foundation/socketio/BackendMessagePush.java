package com.slack.slackjarservice.foundation.socketio;

import com.slack.slackjarservice.foundation.model.dto.SocketMessageDTO;

import java.util.List;

/**
 * Socket消息推送服务接口 后端推送
 */
public interface BackendMessagePush {

    /**
     * 推送消息给批量用户的所有登录端
     *
     * @param userIds 用户ID列表
     * @param message 消息内容
     */
    void pushMessageToUsers(List<String> userIds, SocketMessageDTO message);

    /**
     * 推送消息给单个用户的所有登录端
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    void pushMessageToUser(String userId, SocketMessageDTO message);

    /**
     * 广播消息给所有在线用户
     *
     * @param message 消息内容
     */
    void broadcastMessage(SocketMessageDTO message);
}
