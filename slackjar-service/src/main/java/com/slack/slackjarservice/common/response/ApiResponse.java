package com.slack.slackjarservice.common.response;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局API响应类
 * @author zhn
 */
@Data
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 成功带数据
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResponseEnum.SUCCESS.getCode());
        response.setMessage(ResponseEnum.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    /**
     * 失败
     */
    public static <T> ApiResponse<T> error() {
        return error(ResponseEnum.ERROR.getCode(), ResponseEnum.ERROR.getMessage());
    }

    /**
     * 失败带消息
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(ResponseEnum.ERROR.getCode(), message);
    }

    /**
     * 失败带状态码和消息
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    /**
     * 失败带响应枚举
     */
    public static <T> ApiResponse<T> error(ResponseEnum responseEnum) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(responseEnum.getCode());
        response.setMessage(responseEnum.getMessage());
        response.setData(null);
        return response;
    }
}
