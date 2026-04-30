package com.slack.slackjarservice.common.util;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 字符串工具类
 * @author zhn
 */
@Slf4j
public class StringUtil {

    /**
     * 从完整URL中提取相对路径（去掉域名部分）
     *
     * @param fullUrl 完整URL
     * @return 提取后的相对路径
     */
    public static String extractRelativePath(String fullUrl) {
        return extractRelativePaths(Collections.singletonList(fullUrl)).get(0);
    }

    /**
     * 从完整URL中提取相对路径（去掉域名部分）
     *
     * @param fullUrls 包含完整URL的列表
     * @return 提取后的相对路径列表
     */
    public static List<String> extractRelativePaths(List<String> fullUrls) {
        List<String> relativePaths = new ArrayList<>();

        if (fullUrls == null || fullUrls.isEmpty()) {
            return relativePaths;
        }

        for (String url : fullUrls) {
            try {
                // 解析URL
                URL parsedUrl = new URL(url);
                // 获取路径部分（包含查询参数和锚点）
                String path = parsedUrl.getFile();
                // 如果路径以"/"开头，去掉它
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                relativePaths.add(path);
            } catch (MalformedURLException e) {
                log.error("无效的URL: {}", url);
                throw new BusinessException(ResponseEnum.FILE_URL_INVALID);
            }
        }
        return relativePaths;
    }
}
