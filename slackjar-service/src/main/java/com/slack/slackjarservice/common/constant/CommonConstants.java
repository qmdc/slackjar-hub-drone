package com.slack.slackjarservice.common.constant;

/**
 * 组件常量类
 *
 * @author zhn
 */
public interface CommonConstants {

    /**
     * 登录拦截白名单路径
     */
    String[] SA_TOKEN_EXCLUDE_PATH_PATTERNS = {
            "/sys-user/login/user-name",
            "/sys-user/login/encrypted",
            "/third-party/hitokoto",
            "/ai/chat/stream",
            "/yolo/stream/**"
    };

    /**
     * 角色类型常量
     */
    interface RoleType {
        /**
         * 系统内置角色（不可删除、不可修改编码）
         */
        int SYSTEM = 1;

        /**
         * 自定义角色（可编辑）
         */
        int CUSTOM = 2;
    }

    /**
     * socketIO推送事件类型
     */
    interface SocketIoPushEventType {
        /**
         * 后端推送事件
         */
        String BACKEND_MESSAGE = "backend-message";
    }
}
