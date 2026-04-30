package com.slack.slackjarservice.foundation.aipolicy.doubao;

import com.slack.slackjarservice.common.constant.PromptConstants;
import com.slack.slackjarservice.common.enumtype.foundation.AiChatBizTypeEnum;
import com.slack.slackjarservice.common.enumtype.foundation.AiModelVendorEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.util.RandomUtil;
import com.slack.slackjarservice.foundation.aipolicy.AbstractAiChatStrategy;
import com.slack.slackjarservice.foundation.model.dto.AiDoubaoConfigDTO;
import com.slack.slackjarservice.foundation.model.request.AiChatRequest;
import com.slack.slackjarservice.foundation.model.response.AiChatResponse;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 豆包AI聊天策略实现
 * @author zhn
 */
@Slf4j
@Component
public class DoubaoChatStrategy extends AbstractAiChatStrategy {

    @Resource
    private DoubaoAiConfig doubaoAiConfig;

    @Override
    public AiModelVendorEnum getVendorEnum() {
        return AiModelVendorEnum.DOUBAO_SEED_1_6;
    }

    @Override
    public void refreshConfig() {
        doubaoAiConfig.refresh();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        ArkService arkService = doubaoAiConfig.getArkService();
        AiDoubaoConfigDTO config = doubaoAiConfig.getConfig();

        AiChatBizTypeEnum aiChatBizTypeEnum = AiChatBizTypeEnum.fromCode(request.getAiChatBizType());
        String limitKey = PromptConstants.getBasicChatKey(aiChatBizTypeEnum.getMaxLength());
        String prompt = StringUtils.hasText(request.getPrompt()) ? limitKey : request.getPrompt() + limitKey;

        // prompt消息
        ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(prompt)
                .build();
        // 用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(request.getMessage())
                .build();
        // 消息列表
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(userMessage);
        chatMessages.add(systemMessage);

        // 构建AI请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .messages(chatMessages)
                .model(config.getModelId())
                .maxTokens(Integer.parseInt(config.getMaxTokens()))
                .temperature(Double.parseDouble(config.getTemperature()))
                .thinking(new ChatCompletionRequest.ChatCompletionRequestThinking(config.getDeepThinking()))
                .build();

        try {
            String answer = "";
            String reasoningContent = "";
            Integer totalTokens = 0;
            String model = AiModelVendorEnum.DOUBAO_SEED_1_6.getCode();
            String sessionId = Objects.nonNull(request.getSessionId()) ? request.getSessionId() : RandomUtil.getCurrentSecondStr();

            long startTime = System.currentTimeMillis();
            List<ChatCompletionChoice> choices = arkService.createChatCompletion(chatCompletionRequest).getChoices();
            long endTime = System.currentTimeMillis();
            Integer spendTime = Math.toIntExact((endTime - startTime) / 1000);
            for (ChatCompletionChoice choice : choices) {
                if (choice.getMessage().getReasoningContent() != null) {
                    // 推理内容
                    reasoningContent = choice.getMessage().getReasoningContent().replaceFirst("^\\n", "");
                }
                // 回复内容
                answer = choice.getMessage().getContent().toString();
            }

            return getAiChatResponse(answer, reasoningContent, sessionId, totalTokens, model, spendTime);
        } catch (Exception e) {
            log.error("调用AI服务失败", e);
            return createErrorResponse("调用AI服务失败: " + e.getMessage(), AiModelVendorEnum.DOUBAO_SEED_1_6.getCode());

        }
    }

    private static AiChatResponse getAiChatResponse(String answer, String reasoningContent, String sessionId,
                                                    Integer totalTokens, String model, Integer spendTime) {
        AiChatResponse chatResponse = new AiChatResponse();
        chatResponse.setContent(answer);
        chatResponse.setReasoningContent(reasoningContent);
        chatResponse.setSessionId(sessionId);
        chatResponse.setTokensUsed(totalTokens);
        chatResponse.setTimestamp(System.currentTimeMillis());
        chatResponse.setModel(model);
        chatResponse.setSpendTime(spendTime);
        return chatResponse;
    }

    @Override
    public Flux<String> streamChat(AiChatRequest request) {
        try {
            ArkService arkService = doubaoAiConfig.getArkService();
            AiDoubaoConfigDTO config = doubaoAiConfig.getConfig();

            AiChatBizTypeEnum aiChatBizTypeEnum = AiChatBizTypeEnum.fromCode(request.getAiChatBizType());
            String limitKey = PromptConstants.getBasicChatKey(aiChatBizTypeEnum.getMaxLength());
            String prompt = StringUtils.hasText(request.getPrompt()) ? limitKey : request.getPrompt() + limitKey;

            // 构建历史聊天消息
            List<ChatMessage> chatMessages = buildChatMessages(request, Integer.parseInt(config.getWindowSize()));

            // 添加当前用户消息
            ChatMessage userMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(request.getMessage())
                    .build();
            chatMessages.add(userMessage);

            // 添加系统提示消息（作为最后一条消息）
            ChatMessage systemMessage = ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content(prompt)
                    .build();
            chatMessages.add(systemMessage);

            // 构建AI请求
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .messages(chatMessages)
                    .model(config.getModelId())
                    .maxTokens(Integer.parseInt(config.getMaxTokens()))
                    .temperature(Double.parseDouble(config.getTemperature()))
                    .thinking(new ChatCompletionRequest.ChatCompletionRequestThinking(config.getDeepThinking()))
                    .stream(true)
                    .build();

            return streamMessageFlux(config, arkService, chatCompletionRequest);
        } catch (Exception e) {
            log.error("豆包流式调用初始化失败", e);
            return Flux.error(new BusinessException(ResponseEnum.AI_API_SERVER));
        }
    }

    private static List<ChatMessage> buildChatMessages(AiChatRequest request, Integer windowSize) {
        List<ChatMessage> chatMessages = new ArrayList<>();

        // 如果有对话历史，则添加到消息列表中
        if (request.getChatHistory() != null && !request.getChatHistory().isEmpty()) {
            // 限制对话历史长度，避免超出模型上下文限制
            int startIndex = Math.max(0, request.getChatHistory().size() - windowSize);

            for (int i = startIndex; i < request.getChatHistory().size(); i++) {
                AiChatRequest.ChatMessage historyMessage = request.getChatHistory().get(i);
                ChatMessageRole role = "user".equals(historyMessage.getRole()) ? ChatMessageRole.USER : ChatMessageRole.ASSISTANT;

                ChatMessage chatMessage = ChatMessage.builder()
                        .role(role)
                        .content(historyMessage.getContent())
                        .build();
                chatMessages.add(chatMessage);
            }
        }
        return chatMessages;
    }

    private static Flux<String> streamMessageFlux(AiDoubaoConfigDTO config, ArkService arkService,
                                                  ChatCompletionRequest chatCompletionRequest) {
        return Flux.create(emitter -> {
            try {
                log.info("开始处理豆包流式响应，模型ID: {}", config.getModelId());
                arkService.streamChatCompletion(chatCompletionRequest)
                        .doOnNext(chunk -> {
                            List<ChatCompletionChoice> choices = chunk.getChoices();
                            if (choices != null && !choices.isEmpty()) {
                                ChatCompletionChoice choice = choices.get(0);
                                if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                                    String content = choice.getMessage().getContent().toString();
                                    emitter.next(content);
                                }
                            }
                        })
                        .doOnComplete(emitter::complete)
                        .doOnError(error -> {
                            log.error("豆包流式调用过程中出错", error);
                            emitter.error(new BusinessException(ResponseEnum.AI_API_SERVER));
                        })
                        .subscribe();
            } catch (Exception e) {
                log.error("豆包流式调用初始化失败", e);
                emitter.error(new BusinessException(ResponseEnum.AI_API_SERVER));
            }
        });
    }
}
