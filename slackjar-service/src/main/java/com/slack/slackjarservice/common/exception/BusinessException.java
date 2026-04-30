package com.slack.slackjarservice.common.exception;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import lombok.Getter;

/**
 * 自定义业务异常类
 * @author zhn
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    public BusinessException(ResponseEnum responseEnum) {
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }

    public BusinessException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this.code = ResponseEnum.ERROR.getCode();
        this.message = message;
    }
}
