package com.slack.slackjarservice.foundation.model.request;

import com.slack.slackjarservice.common.base.BasePagination;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户设备登录记录分页查询请求参数
 *
 * @author zhn
 * @since 2025-04-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDevicePageQuery extends BasePagination {

    /**
     * 设备类型（PC/MAC/MOBILE）
     */
    private String device;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 生效状态（0-生效、1-失效）
     */
    private Integer status;
}
