package com.slack.slackjarservice.common.enumtype.foundation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 系统内置角色编码枚举
 *
 * @author zhn
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum {

    /**
     * 超级管理员
     */
    ROLE_SUPER_ADMIN("ROLE_SUPER_ADMIN", "超级管理员"),

    /**
     * 普通用户
     */
    ROLE_USER("ROLE_USER", "普通用户"),
    ;

    private final String code;

    private final String description;

    @Override
    public String toString() {
        return code;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 角色编码
     * @return 角色枚举
     */
    public static RoleCodeEnum getByCode(String code) {
        return Arrays.stream(values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
    }

    /**
     * 获取所有系统内置角色编码列表
     *
     * @return 角色编码列表
     */
    public static List<String> getAllCodes() {
        return Arrays.stream(values()).map(RoleCodeEnum::getCode).toList();
    }
}
