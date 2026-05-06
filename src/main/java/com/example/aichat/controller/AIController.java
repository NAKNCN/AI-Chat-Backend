package com.example.aichat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aichat.pojo.Result;
import com.example.aichat.pojo.ChatRequest;
import com.example.aichat.pojo.ChatHistory;
import com.example.aichat.service.ChatHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
public class AIController {

    @Value("${ai.api-key}")
    private String apiKey;
    @Value("${ai.api-url}")
    private String apiUrl;
    private final ChatHistoryService chatHistoryService;

    private OkHttpClient client;
    private static final ObjectMapper mapper = new ObjectMapper(); // mapper本身不依赖配置，可以保留static

    @PostConstruct
    public void init() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    // 添加构造方法，Spring 自动注入
    public AIController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }


    private String parseAnswer(String responseStr) {
        try {
            JsonNode jsonNode = mapper.readTree(responseStr);
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "解析 AI 回答失败";
        }
    }

//    private String parseAnswer(String responseStr) {
//        try {
//            JsonNode jsonNode = mapper.readTree(responseStr);
//            String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
//    // 调用格式化方法清洗 Markdown 符号
//            return formatContent(content);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "解析 AI 回答失败";
//        }
//    }

//    private String formatContent(String content) {
//        if (content == null || content.isEmpty()) {
//            return content;
//        }
//        // 1. 去除加粗标记 *text*
//        content = content.replaceAll("\\*(.*?)\\*", "$1");
//        // 2. 去除斜体标记 *text*
//        content = content.replaceAll("\\*(.*?)\\*", "$1");
//        // 3. 去除列表标记 - 开头（行首的 - 替换为空）
//        content = content.replaceAll("(?m)^-\\s+", "");
//        // 4. 去除表情符号和特殊符号（如 ✅ ❌ ⚠️ 💡）
//        content = content.replaceAll("[✅❌⚠️💡▶]", "");
//        // 5. 去除多余空行（连续两个以上换行替换为一个换行）
//        content = content.replaceAll("\n{3,}", "\n\n");
//        // 6. 去除首尾空白
//        content = content.trim();
//        return content;
//    }

    @GetMapping("/ai/test")
    public Result testAI(@RequestParam(defaultValue = "你好") String prompt) {
        try {
            // 使用 Jackson 构建 JSON 请求体
            ObjectNode root = mapper.createObjectNode();
            root.put("model", "qwen-plus");

            ArrayNode messages = mapper.createArrayNode();
            ObjectNode userMessage = mapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);

            root.set("messages", messages);
            String jsonBody = mapper.writeValueAsString(root);
            //String json = "{ \"f1\" : \"v1\" } ";
            // 构建 OkHttp 请求
            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    return Result.success(responseStr);
                } else {
                    return Result.fail(500, "AI调用失败，状态码：" + response.code());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(500, "网络异常：" + e.getMessage());
        }
    }

    @PostMapping("/ai/chat")
    public Result chat(@RequestBody ChatRequest chatRequest) { // [修改点]
            Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String question = chatRequest.getQuestion();
        try {
            // 构建请求 JSON（与 testAI 相同）
            ObjectNode root = mapper.createObjectNode();
            root.put("model", "qwen-plus");
            ArrayNode messages = mapper.createArrayNode();
            messages.addObject().put("role", "user").put("content", question);
            root.set("messages", messages);
            String jsonBody = mapper.writeValueAsString(root);
            System.out.println("请求体: " + jsonBody);
            // 发送请求
            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // 获取响应并解析
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    String answer = parseAnswer(responseStr);

                   // 保存对话记录
                    ChatHistory record = new ChatHistory();
                    record.setUserId(userId);
                    record.setQuestion(question);
                    record.setAnswer(answer);
                    chatHistoryService.saveRecord(record);

                    return Result.success(answer);
                } else {
                    return Result.fail(500, "AI调用失败，状态码：" + response.code());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(500, "网络异常：" + e.getMessage());
        }
    }

    @GetMapping("/chat/history")
    public Result getHistory(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<ChatHistory> page = chatHistoryService.getHistoryByUserId(userId, pageNum, pageSize);
        return Result.success(page);
    }
}