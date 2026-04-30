package com.slack.slackjarservice.foundation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.foundation.model.response.HitokotoResponse;
import com.slack.slackjarservice.foundation.model.response.IpInfoResponse;
import com.slack.slackjarservice.foundation.service.ThirdPartyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 第三方服务实现类
 */
@Slf4j
@Service
public class ThirdPartyServiceImpl implements ThirdPartyService {

    @Resource
    private OkHttpClient okHttpClient;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public HitokotoResponse getHitokoto() {
        Request request = new Request.Builder()
                .url("https://v1.hitokoto.cn/")
                .get()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Map<String, Object> hitokotoMap = objectMapper.readValue(responseBody, HashMap.class);
                return new HitokotoResponse(
                        (String) hitokotoMap.get("hitokoto"),
                        (String) hitokotoMap.get("from")
                );
            }
        } catch (IOException e) {
            log.error("获取一言数据失败", e);
        }

        // 出错时返回默认值
        return new HitokotoResponse("如果你能在浪费时间中获得乐趣，就不算浪费时间", "Slackjar");
    }

    @Override
    public IpInfoResponse getIpInfo(String ip) {
        // 本地IP不进行解析
        if (isLocalIp(ip)) {
            return new IpInfoResponse(ip, "本地网络");
        }

        Request request = new Request.Builder()
                .url("http://ip-api.com/json/" + ip + "?lang=zh-CN")
                .get()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Map<String, Object> ipInfoMap = objectMapper.readValue(responseBody, HashMap.class);
                if (ResponseEnum.SUCCESS.getMessage().equals(ipInfoMap.get("status"))) {
                    return new IpInfoResponse(ip, (String) ipInfoMap.get("regionName"));
                } else {
                    log.warn("IP解析失败: {}", ipInfoMap.get("message"));
                    return new IpInfoResponse(ip, "未知地区");
                }
            }
        } catch (IOException e) {
            log.error("IP解析异常: {}", e.getMessage(), e);
        }

        return new IpInfoResponse(ip, "未知地区");
    }

    /**
     * 判断是否为本地IP
     * @param ip IP地址
     * @return 是否为本地IP
     */
    private boolean isLocalIp(String ip) {
        return ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") ||
               ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.");
    }
}
