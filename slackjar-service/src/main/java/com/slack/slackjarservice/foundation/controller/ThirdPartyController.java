package com.slack.slackjarservice.foundation.controller;

import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.foundation.model.response.HitokotoResponse;
import com.slack.slackjarservice.foundation.model.response.IpInfoResponse;
import com.slack.slackjarservice.foundation.service.ThirdPartyService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhn
 */
@RestController
@RequestMapping("/third-party")
public class ThirdPartyController extends BaseController {

    @Resource
    private ThirdPartyService thirdPartyService;

    @GetMapping("/hitokoto")
    public ApiResponse<HitokotoResponse> getHitokoto() {
        HitokotoResponse response = thirdPartyService.getHitokoto();
        recordOperateLog(OperationEnum.OTHER, "获取一言成功：" + response.toString());
        return success(response);
    }

    @GetMapping("/ip-info/{ip}")
    public ApiResponse<IpInfoResponse> getIpInfo(@PathVariable String ip) {
        IpInfoResponse response = thirdPartyService.getIpInfo(ip);
        recordOperateLog(OperationEnum.OTHER, "获取IP信息成功: " + response.toString());
        return success(response);
    }

}
