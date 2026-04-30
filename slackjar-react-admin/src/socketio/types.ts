/**
 * Socket消息推送DTO
 */
export interface SocketMessageDTO {
    // 推送毫秒级时间戳
    timestamp: number;
    // 业务类型
    bizType: string;
    // 推送内容
    content: any;
}

/**
 * Socket消息业务类型枚举（后端推送给前端）
 */
export enum PushWithBackendEnum {
    // 成功-普通字符串通知消息
    SUCCESS_STRING_NOTICE = 'SUCCESS_STRING_NOTICE',
    // 失败-普通字符串通知消息
    FAIL_STRING_NOTICE = 'FAIL_STRING_NOTICE',
    // IP地级市信息
    IP_CITY_INFO = 'IP_CITY_INFO',
}

/**
 * Socket消息业务类型枚举（前端推送给后端）
 */
export enum PushWithFrontEnum {
    // 系统通知
    SYSTEM_NOTICE = 'SYSTEM_NOTICE',
    // 用户消息
    USER_MESSAGE = 'USER_MESSAGE',
    // 业务提醒
    BUSINESS_REMINDER = 'BUSINESS_REMINDER',
}

/**
 * 消息处理器接口
 */
export interface MessageHandler {
    (message: SocketMessageDTO): void;
}
