package com.example.aichat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aichat.pojo.Result;
import com.example.aichat.pojo.ChatRequest;
import com.example.aichat.pojo.ChatHistory;
import com.example.aichat.service.ChatHistoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class AIController {

    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;

    private static final int CONTEXT_LIMIT = 10;

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    public AIController(ChatClient.Builder chatClientBuilder,
                        ChatHistoryService chatHistoryService) {
        this.chatClient = chatClientBuilder.build();
        this.chatHistoryService = chatHistoryService;
    }

    // ========== 原有接口不变 ==========
    @GetMapping("/ai/test")
    public Result testAI(@RequestParam(defaultValue = "你好") String prompt) {
        String reply = chatClient.prompt(prompt).call().content();
        return Result.success(reply);
    }

    @PostMapping("/ai/chat")
    public Result chat(@RequestBody ChatRequest chatRequest) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String question = chatRequest.getQuestion();

        List<ChatHistory> history = chatHistoryService.getRecentHistory(userId, CONTEXT_LIMIT);
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("你是一个有用的AI助手，请根据对话历史回答用户的问题。"));

        for (ChatHistory record : history) {
            messages.add(new UserMessage(record.getQuestion()));
            messages.add(new AssistantMessage(record.getAnswer()));
        }

        messages.add(new UserMessage(question));

        String answer = chatClient.prompt()
                .messages(messages)
                .call()
                .content();

        ChatHistory record = new ChatHistory();
        record.setUserId(userId);
        record.setQuestion(question);
        record.setAnswer(answer);
        chatHistoryService.saveRecord(record);

        return Result.success(answer);
    }

    @GetMapping("/chat/history")
    public Result getHistory(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<ChatHistory> page = chatHistoryService.getHistoryByUserId(userId, pageNum, pageSize);
        return Result.success(page);
    }

    // ========== 新增流式聊天接口（修正版） ==========
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
        // 1. 直接从 SecurityContext 获取 userId（与原有方式一致）
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. 获取最近对话历史
        List<ChatHistory> recentHistory = chatHistoryService.getRecentHistory(userId, CONTEXT_LIMIT);
        List<Message> historyMessages = new ArrayList<>();
        for (ChatHistory record : recentHistory) {
            historyMessages.add(new UserMessage(record.getQuestion()));
            historyMessages.add(new AssistantMessage(record.getAnswer()));
        }

        // 3. 添加当前用户问题
        String question = chatRequest.getQuestion();   // ChatRequest 中有 getQuestion()，与原有一致
        historyMessages.add(new UserMessage(question));

        // 4. 先保存用户问题（answer 暂时为空）
        ChatHistory userRecord = new ChatHistory();
        userRecord.setUserId(userId);
        userRecord.setQuestion(question);
        userRecord.setAnswer("");  // AI 回答先占位，流结束后更新
        chatHistoryService.saveRecord(userRecord);

        // 5. 构建流式回答并收集完整答案
        StringBuilder fullAnswer = new StringBuilder();

        Flux<String> stream = chatClient.prompt()
                .messages(historyMessages)
                .stream()
                .content();

        // 注意：这里不能直接更新之前保存的记录，因为 id 需要获取。
        // 简单方案：保存一个新记录，或者在前端完成后通过回调更新。
        // 为简化，我们在流完成时直接新插入一条完整的 AI 回答记录。
        return stream
                .doOnNext(fullAnswer::append)
                .doOnComplete(() -> {
                    ChatHistory assistantRecord = new ChatHistory();
                    assistantRecord.setUserId(userId);
                    assistantRecord.setQuestion("");   // AI 回答没有问题
                    assistantRecord.setAnswer(fullAnswer.toString());
                    chatHistoryService.saveRecord(assistantRecord);
                });
    }
}