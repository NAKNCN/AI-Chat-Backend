package com.example.aichat.controller;

import com.example.aichat.pojo.Result;
import com.example.aichat.service.KnowledgeService;   // 改为接口
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;   // 注入接口

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/add")
    public Result addDocument(@RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        if (content == null || content.isBlank()) {
            return Result.error(400, "文档内容不能为空");
        }
        Map<String, Object> metadata = (Map<String, Object>) body.getOrDefault("metadata", Map.of());
        try {
            knowledgeService.addDocument(content, metadata);
            return Result.success("文档添加成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteDocument(@PathVariable String id) {
        boolean deleted = knowledgeService.deleteDocument(id);
        return deleted ? Result.success("删除成功") : Result.error(404, "文档不存在");
    }

    @GetMapping("/list")
    public Result listDocuments() {
        List<Document> docs = knowledgeService.listDocuments();
        List<Map<String, Object>> result = docs.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>(doc.getMetadata());
            map.put("content", doc.getText());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
}