package com.slack.slackjarservice.common.base;

import cn.dev33.satoken.stp.StpUtil;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.foundation.entity.SysUser;
import com.slack.slackjarservice.foundation.service.SysUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 控制器基类，封装通用方法
 *
 * @author zhn
 */
@Slf4j
public abstract class BaseController {

    @Resource
    private SysUserService sysUserService;

    @Resource(name = "operateLogExecutor")
    private Executor operateLogExecutor;

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户ID
     */
    protected Long getLoginUserId() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResponseEnum.UN_LOGIN);
        }
        return Long.valueOf(StpUtil.getLoginId().toString());
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户实体
     */
    protected SysUser getLoginUser() {
        Long userId = getLoginUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResponseEnum.USER_NOT_EXIST);
        }
        // 防止密码泄露，清空密码字段
        user.setPassword(null);
        return user;
    }

    /**
     * 处理参数校验结果
     *
     * @param bindingResult 参数校验结果
     */
    protected void handleValidationResult(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            StringBuilder errorMsg = new StringBuilder();
            for (ObjectError error : errors) {
                errorMsg.append(error.getDefaultMessage()).append(";");
            }
            throw new BusinessException(ResponseEnum.PARAM_ERROR.getCode(), errorMsg.toString());
        }
    }

    /**
     * 构建成功响应
     *
     * @param data 响应数据
     * @return 统一响应对象
     */
    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * 构建成功响应（无数据）
     *
     * @return 统一响应对象
     */
    protected <T> ApiResponse<T> success() {
        return ApiResponse.success();
    }

    /**
     * 构建错误响应
     *
     * @param message 错误消息
     * @return 统一响应对象
     */
    protected <T> ApiResponse<T> error(String message) {
        return ApiResponse.error(message);
    }

    /**
     * 构建错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 统一响应对象
     */
    protected <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.error(code, message);
    }

    /**
     * 记录操作日志（异步执行）
     *
     * @param operation 操作类型枚举
     * @param content   操作内容
     */
    protected void recordOperateLog(OperationEnum operation, String content) {
        // 不记录指定操作日志
        Set<OperationEnum> skipOperateSet = Set.of(OperationEnum.USER_QUERY);
        if (skipOperateSet.contains(operation)) {
            return;
        }
        SysUser loginUser = getLoginUser();
        operateLogExecutor.execute(() -> {
            log.info("用户[{}-{}]执行操作[{}]：{}", loginUser.getUsername(), loginUser.getId(), operation.getDesc(), content);
        });
    }
}
