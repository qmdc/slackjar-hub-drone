package com.slack.slackjarservice.common.constant;

/**
 * 登录相关常量
 * @author zhn
 */
public class LoginConstants {

    /**
     * 登录错误次数key前缀
     */
    public static final String LOGIN_ERROR_COUNT_PREFIX = "login:error:count:";

    /**
     * 用户登录次数key前缀
     */
    public static final String LOGIN_COUNT_PREFIX = "login:count:";

    /**
     * 登录错误次数超时时间（秒）
     */
    public static final long LOGIN_ERROR_TIMEOUT = 1800;

    /**
     * 最大登录错误次数
     */
    public static final int MAX_LOGIN_ERROR_COUNT = 5;

    /**
     * 获取登录错误次数的key
     * @param username 用户名
     * @return key
     */
    public static String getLoginErrorCountKey(String username) {
        return LOGIN_ERROR_COUNT_PREFIX + username;
    }

    /**
     * 获取用户登录次数的key
     * @param userId 用户ID
     * @return key
     */
    public static String getLoginCountKey(Long userId) {
        return LOGIN_COUNT_PREFIX + userId;
    }
}
