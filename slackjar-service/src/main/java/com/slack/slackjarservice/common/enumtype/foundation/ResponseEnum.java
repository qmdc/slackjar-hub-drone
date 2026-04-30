package com.slack.slackjarservice.common.enumtype.foundation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态枚举
 *
 * @author zhn
 */
@Getter
@AllArgsConstructor
public enum ResponseEnum {

    /**
     * HTTP状态码相关(100-599)
     */
    SUCCESS(200, "success"),
    ERROR(500, "error"),
    PARAM_ERROR(400, "参数错误"),
    UN_LOGIN(401, "用户登录无效"),
    NO_PERMISSION(403, "暂无操作权限"),
    NOT_FOUND(404, "资源不存在"),
    RATE_LIMIT(429, "系统繁忙，请稍后再试"),
    SERVICE_DEGRADE(503, "服务暂不可用，请稍后再试"),

    /**
     * 通用错误相关 (600-699)
     */
    DATA_INVALID(600, "error.data_invalid"),
    NOT_FOUND_ERROR(601, "error.not_found"),
    ERROR_GENERAL(602, "error.error"),
    ERROR_NET(603, "error.network"),
    OPTIMISTIC_LOCK(604, "error.optimistic_lock"),
    DATA_EXISTS(605, "error.data_exists"),
    DATA_NOT_EXISTS(606, "error.data_not_exists"),
    FORBIDDEN(607, "error.forbidden"),
    ERROR_CODE_REPEAT(608, "error.code_repeat"),
    ERROR_NUMBER_REPEAT(609, "error.number_repeat"),
    ERROR_SQL_EXCEPTION(610, "error.sql_exception"),
    NOT_LOGIN(611, "error.not_login"),
    NOT_NULL(612, "error.not_null"),
    TIMEOUT(613, "error.timeout"),
    SERVER_BUSY(614, "error.serverBusy"),

    /**
     * 用户管理相关(1000-1030)
     */
    USER_EXIST(1000, "用户已存在"),
    USER_NOT_EXIST(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_NOT_LOGIN(1004, "用户未登录"),
    PASSWORD_NOT_MATCH(1005, "两次密码输入不一致"),
    NO_PERMISSION_LOGIN_INFO(1006, "暂无权限操作用户登陆信息"),
    ORIGINAL_PASSWORD_ERROR(1007, "原始密码错误"),

    /**
     * 角色管理相关(1031-1060)
     */
    ROLE_CODE_NOT_ALLOW_MODIFY(1031, "角色编码不允许修改"),
    ROLE_SYSTEM_NOT_ALLOW_DISABLE(1032, "系统内置角色不允许禁用"),
    ROLE_SYSTEM_NOT_ALLOW_DELETE(1033, "系统内置角色不允许删除"),
    ROLE_CODE_REPEAT(1034, "角色编码不能重复"),

    /**
     * 权限管理相关(1061-1099)
     */
    PERMISSION_CODE_NOT_ALLOW_MODIFY(1061, "权限编码不允许修改"),
    PERMISSION_CODE_REPEAT(1062, "权限编码不能重复"),
    PERMISSION_CODE_EXITS_SUB_LEVEL(1063, "当前权限存在子权限不可删除"),

    /**
     * 文件管理相关(1100-1199)
     */
    FILE_EXCEED(1100, "文件大小超出限制"),
    FILE_UPLOAD(1101, "文件上传异常"),
    FILE_DOWNLOAD(1102, "文件下载异常"),
    FILE_SIGNATURE(1103, "文件签名获取异常"),
    FILE_DELETE(1104, "文件删除异常"),
    FILE_CALLBACK(1105, "文件上传回调异常"),
    FILE_DOMAIN(1106, "请配置文件上传域名"),
    FILE_STORAGE_STRATEGY(1107, "未找到可用的文件存储策略"),
    FILE_BIZ_TYPE(1108, "文件业务类型不正确"),
    FILE_FORMAT_NOT_ALLOWED(1109, "文件格式不允许"),
    FILE_NOT_EMPTY(1110, "文件不能为空"),
    FILE_NOT_EXIST(1111, "文件不存在"),
    FILE_URL_INVALID(1112, "无效的URL"),
    FILE_ACCESS_NOT(1113, "暂无当前文件下载权限"),
    FILE_READER_STREAM(1114, "文件流读取失败"),

    /**
     * AI相关(1200-1299)
     */
    AI_INVALID(1200, "未找到可用的AI模型"),
    AI_CONFIG(1201, "AI参数未配置"),
    AI_CONFIG_REFRESH(1202, "AI参数刷新失败"),
    AI_CLIENT(1203, "创建AI客户端失败"),
    AI_CLIENT_REFRESH(1204, "刷新AI客户端失败"),
    AI_API_SERVER(1205, "AI服务调用失败"),
    AI_CHAT_PROCESS(1206, "未找到可用的AI对话处理器"),
    AI_CHAT_MESSAGE_LENGTH(1207, "AI对话请求文本超限"),
    AI_CHAT_MESSAGE_TYPE(1208, "AI对话请求业务类型不存在"),
    AI_CHAT_STREAM_SSE_TIMEOUT(1209, "SSE连接超时"),

    /**
     * 安全与通信相关 (1300-1399)
     */
    SSL_CERT_SOURCE_INVALID(1300, "无效的证书来源编码"),
    SSL_CERT_UPLOAD_VERIFY_FAIL(1301, "证书文件上传后验证失败"),
    SSL_CERT_SERVER_CONFIG_EMPTY(1302, "服务器参数配置为空"),
    SSL_CERT_SFTP_CONNECT_FAIL(1303, "创建 SFTP 连接失败"),
    SSL_CERT_SFTP_UPLOAD_FAIL(1304, "SSL 证书上传失败"),
    SSL_CERT_NOT_FOUND(1305, "SSL 证书不存在"),

    RSA_ENCRYPT_ERROR(1326, "RSA 加密失败"),
    RSA_DECRYPT_ERROR(1327, "RSA 解密失败"),
    RSA_LOAD_PUBLIC_KEY_ERROR(1328, "加载 RSA 公钥失败"),
    RSA_LOAD_PRIVATE_KEY_ERROR(1329, "加载 RSA 私钥失败"),

    SOCKET_BIZ_NOT_FOUND(1351, "消息业务类型不存在"),

    /**
     * 配置与字典相关 (1400-1499)
     */
    DICT_CODE_NOT_ALLOW_MODIFY(1400, "字典编码不允许修改"),
    DICT_CODE_REPEAT(1401, "字典编码已存在"),
    DICT_CODE_ITEM_REPEAT(1402, "字典项编码不能重复"),
    DICT_CODE_NOT_EXIST(1403, "字典不存在"),

    /**
     * 其他(9000-9999)
     */
    SHELL_COMMAND_ERROR(9000, "Shell命令执行异常"),
    ;

    private final int code;
    private final String message;
}
