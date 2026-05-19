package com.example.aichat.controller;

import com.example.aichat.pojo.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/ai")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder chatClientBuilder,
                         VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @PostMapping("/rag")
    public Result ragChat(@RequestParam String question) {
        // 1. 构建搜索请求（使用 Builder 代替弃用的 query 方法）
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(3)
                .build();

        // 2. 语义检索
        List<Document> similarDocs = vectorStore.similaritySearch(request);

        // 3. 拼接上下文（使用 getText 代替弃用的 getContent）
        String context = Objects.requireNonNull(similarDocs).stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 4. 构建提示词
        String prompt = """
                请根据以下参考信息回答用户问题。
                如果参考信息中没有明确答案，请据实回答"未找到相关信息"。
                
                参考信息：
                %s
                
                用户问题：%s
                """.formatted(context, question);

        String answer = chatClient.prompt(prompt).call().content();
        return Result.success(answer);
    }
}