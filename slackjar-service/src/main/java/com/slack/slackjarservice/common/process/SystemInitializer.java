package com.slack.slackjarservice.common.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 系统初始化
 * @author zhn
 */
@Slf4j
@Component
public class SystemInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info("应用启动完成");
    }
}
