package com.slack.slackjarservice.foundation.service.impl;

import com.slack.slackjarservice.foundation.model.request.AiChatRequest;
import com.slack.slackjarservice.foundation.model.response.AiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

@Slf4j
@SpringBootTest
@SuppressWarnings("all")
class AiServiceImplTest {

    @Resource
    private AiServiceImpl aiService;

    @Test
    public void testChat() {
        AiChatRequest request = new AiChatRequest();
        request.setMessage("你好，世界");
        request.setSessionId("test-session-id");

        AiChatResponse response = aiService.chat(request);

        System.out.println("AI回复: " + response.getContent());

        // 等待异步任务完成
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void generateChatVectorTest() {
    }

}
