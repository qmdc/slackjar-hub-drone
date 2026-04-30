package com.slack.slackjarservice.common.util;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * 断言工具类，用于业务逻辑校验
 * @author zhn
 */
public class AssertUtil {

    /**
     * 断言表达式为true，否则抛出业务异常
     * @param expression 表达式
     * @param responseEnum 响应枚举
     */
    public static void isTrue(boolean expression, ResponseEnum responseEnum) {
        if (!expression) {
            throw new BusinessException(responseEnum);
        }
    }

    /**
     * 断言表达式为true，否则抛出业务异常
     * @param expression 表达式
     * @param code 错误码
     * @param message 错误信息
     */
    public static void isTrue(boolean expression, int code, String message) {
        if (!expression) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 断言对象不为null，否则抛出业务异常
     * @param object 对象
     * @param responseEnum 响应枚举
     */
    public static void notNull(Object object, ResponseEnum responseEnum) {
        if (object == null) {
            throw new BusinessException(responseEnum);
        }
    }

    /**
     * 断言对象为null，否则抛出业务异常
     * @param object 对象
     * @param responseEnum 响应枚举
     */
    public static void isNull(Object object, ResponseEnum responseEnum) {
        if (object != null) {
            throw new BusinessException(responseEnum);
        }
    }

    /**
     * 断言集合不为空，否则抛出业务异常
     * @param list 集合
     * @param responseEnum 响应枚举
     */
    public static void notEmpty(List<?> list, ResponseEnum responseEnum) {
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(responseEnum);
        }
    }

    /**
     * 断言字符串不为空，否则抛出业务异常
     * @param str 字符串
     * @param responseEnum 响应枚举
     */
    public static void notEmpty(String str, ResponseEnum responseEnum) {
        if (str == null || str.isEmpty()) {
            throw new BusinessException(responseEnum);
        }
    }

    /**
     * 断言两个对象相等，否则抛出业务异常
     * @param obj1 对象1
     * @param obj2 对象2
     * @param responseEnum 响应枚举
     */
    public static void equals(Object obj1, Object obj2, ResponseEnum responseEnum) {
        if (!Objects.equals(obj1, obj2)) {
            throw new BusinessException(responseEnum);
        }
    }

    /**
     * 断言两个对象不相等，否则抛出业务异常
     * @param obj1 对象1
     * @param obj2 对象2
     * @param responseEnum 响应枚举
     */
    public static void notEquals(Object obj1, Object obj2, ResponseEnum responseEnum) {
        if (Objects.equals(obj1, obj2)) {
            throw new BusinessException(responseEnum);
        }
    }
}
