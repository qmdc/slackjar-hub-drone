package com.slack.slackjarservice.foundation.service;

import com.slack.slackjarservice.foundation.model.response.HitokotoResponse;
import com.slack.slackjarservice.foundation.model.response.IpInfoResponse;

/**
 * 第三方服务接口
 */
public interface ThirdPartyService {

    /**
     * 获取一言
     * @return 一言响应
     */
    HitokotoResponse getHitokoto();

    /**
     * 根据IP地址获取城市信息
     * @param ip IP地址
     * @return IP信息响应
     */
    IpInfoResponse getIpInfo(String ip);
}
