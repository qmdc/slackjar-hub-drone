package com.slack.slackjarservice.foundation.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.config.SentinelConfig;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.foundation.model.request.AiChatRequest;
import com.slack.slackjarservice.foundation.model.response.AiChatResponse;
import com.slack.slackjarservice.foundation.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * AI控制器
 *
 * @author slackjar
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Validated
public class AiController extends BaseController {

    private final AiService aiService;

    /**
     * 基础对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat")
    @SentinelResource(value = "ai-chat", blockHandlerClass = SentinelConfig.class, blockHandler = "handleBlock")
    public ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse chatResponse = aiService.chat(request);
        recordOperateLog(OperationEnum.AI_CHAT, "AI对话成功，请求内容: " + request.getMessage() + "，响应结果：" + chatResponse.toString());
        return success(chatResponse);
    }

    /**
     * 流式对话
     *
     * @param request 对话请求
     * @return SSE流式响应
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SentinelResource(value = "ai-chat", blockHandlerClass = SentinelConfig.class, blockHandler = "handleBlock")
    public SseEmitter streamChat(@Valid @RequestBody AiChatRequest request) {
        // 记录操作日志
        recordOperateLog(OperationEnum.AI_CHAT_STREAM, "开始AI流式对话，请求内容: " + request.getMessage());

        // 设置超时时间为60秒
        SseEmitter emitter = new SseEmitter(60000L);

        // 添加连接关闭监听器
        emitter.onTimeout(() -> {
            log.error("SSE连接超时，请求内容: {}", request);
            emitter.completeWithError(new BusinessException(ResponseEnum.AI_CHAT_STREAM_SSE_TIMEOUT));
        });

        emitter.onError(e -> {
            log.error("SSE连接异常", e);
        });

        emitter.onCompletion(() -> {
            log.info("SSE连接已完成，请求内容: {}", request.getMessage());
        });

        // 异步处理流式响应
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始处理流式对话请求");
                aiService.streamChat(request)
                        .doOnNext(content -> {
                            try {
                                if (content != null && !content.trim().isEmpty()) {
                                    emitter.send(content);
                                }
                            } catch (IOException e) {
                                log.error("发送流式响应失败", e);
                                emitter.completeWithError(e);
                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                // 发送结束标记
                                emitter.send("[DONE]\n\n");
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("发送流式响应结束标记失败", e);
                                emitter.completeWithError(e);
                            }
                        })
                        .doOnError(error -> {
                            log.error("流式对话处理异常", error);
                            emitter.completeWithError(error);
                        })
                        .subscribe();
            } catch (Exception e) {
                log.error("处理流式对话请求失败", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
