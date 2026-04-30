package com.slack.slackjarservice.common.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * TraceId 配置类
 * 注册 TraceId 拦截器
 *
 * @author zhn
 */
@Configuration
public class TraceConfig implements WebMvcConfigurer {

    @Resource
    private TraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/**");
    }
}
