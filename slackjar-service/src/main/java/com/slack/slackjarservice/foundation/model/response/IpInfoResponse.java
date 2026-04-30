package com.slack.slackjarservice.foundation.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IP信息响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpInfoResponse {
    private String ip;
    private String city;
}