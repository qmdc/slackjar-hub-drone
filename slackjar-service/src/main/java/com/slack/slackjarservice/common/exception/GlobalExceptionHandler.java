package com.slack.slackjarservice.common.exception;

import cn.dev33.satoken.exception.NotRoleException;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * 全局异常处理类
 * @author zhn
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMsg = new StringBuilder();
        for (ObjectError error : bindingResult.getAllErrors()) {
            errorMsg.append(error.getDefaultMessage()).append(";");
        }
        log.error("参数校验异常: {}", errorMsg, e);
        return ApiResponse.error(ResponseEnum.PARAM_ERROR.getCode(), errorMsg.toString());
    }

    /**
     * 处理未登录异常
     */
    @ExceptionHandler(cn.dev33.satoken.exception.NotLoginException.class)
    public ApiResponse<Void> handleNotLoginException(cn.dev33.satoken.exception.NotLoginException e) {
        log.error("未登录异常: {}", e.getMessage(), e);
        return ApiResponse.error(ResponseEnum.UN_LOGIN.getCode(), ResponseEnum.UN_LOGIN.getMessage());
    }

    /**
     * 处理无权限异常
     */
    @ExceptionHandler(cn.dev33.satoken.exception.NotPermissionException.class)
    public ApiResponse<Void> handleNotPermissionException(cn.dev33.satoken.exception.NotPermissionException e) {
        log.error("无权限异常: {}", e.getMessage(), e);
        return ApiResponse.error(ResponseEnum.NO_PERMISSION.getCode(), ResponseEnum.NO_PERMISSION.getMessage());
    }

    /**
     * 处理无角色异常
     */
    @ExceptionHandler(value = NotRoleException.class)
    public ApiResponse<Void> notRoleException(NotRoleException e, HttpServletRequest request) {
        log.error("角色认证出现异常:{},异常类型:{},请求的url:{}", e.getMessage(), e.getClass(), request.getRequestURL());
        return ApiResponse.error(ResponseEnum.NO_PERMISSION.getCode(), ResponseEnum.NO_PERMISSION.getMessage());
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("请求方法不支持: {} {}, 支持的方法: {}", request.getMethod(), request.getRequestURL(), e.getSupportedMethods(), e);
        String[] strings = Objects.nonNull(e.getSupportedMethods()) ? e.getSupportedMethods() : new String[]{};
        return ApiResponse.error(ResponseEnum.PARAM_ERROR.getCode(), "请求方法 '" + e.getMethod() + "' 不被支持，请使用 "
                + String.join(", ", strings) + " 方法");
    }

    /**
     * 处理 Sentinel 限流异常
     */
    @ExceptionHandler(FlowException.class)
    public ApiResponse<Void> handleFlowException(FlowException e, HttpServletRequest request) {
        log.warn("Sentinel 限流触发: {} {}", request.getMethod(), request.getRequestURL());
        return ApiResponse.error(ResponseEnum.RATE_LIMIT);
    }

    /**
     * 处理 Sentinel 熔断降级异常
     */
    @ExceptionHandler(DegradeException.class)
    public ApiResponse<Void> handleDegradeException(DegradeException e, HttpServletRequest request) {
        log.warn("Sentinel 熔断触发: {} {}", request.getMethod(), request.getRequestURL());
        return ApiResponse.error(ResponseEnum.SERVICE_DEGRADE);
    }

    /**
     * 处理 Sentinel 其他阻塞异常
     */
    @ExceptionHandler(BlockException.class)
    public ApiResponse<Void> handleBlockException(BlockException e, HttpServletRequest request) {
        log.warn("Sentinel 阻塞异常: {} {}, type: {}", request.getMethod(), request.getRequestURL(), e.getClass().getSimpleName());
        return ApiResponse.error(ResponseEnum.RATE_LIMIT);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        if (isClientDisconnected(e)) {
            log.warn("客户端断开连接: {}", e.getMessage());
            return null;
        }
        log.error("系统异常: {}", e.getMessage(), e);
        return ApiResponse.error(ResponseEnum.ERROR.getCode(), ResponseEnum.ERROR.getMessage());
    }

    private boolean isClientDisconnected(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        return msg.contains("Connection reset") || msg.contains("Broken pipe") ||
                (e.getCause() != null && (e.getCause().getMessage() != null &&
                        (e.getCause().getMessage().contains("Connection reset") || e.getCause().getMessage().contains("Broken pipe"))));
    }
}
