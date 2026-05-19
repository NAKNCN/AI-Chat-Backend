package com.example.aichat.controller;

import com.example.aichat.pojo.Result;
import com.example.aichat.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/ai/knowledge")
public class FileUploadController {

    @Autowired
    private KnowledgeService knowledgeService;

    @PostMapping("/upload")
    public Result uploadMarkdown(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
            return Result.error(400, "仅支持 Markdown (.md) 文件");
        }
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            // 按一级标题或空行切分
            String[] blocks = content.split("(?=^#{1,6}\\s)|\\n{2,}");
            int count = 0;
            for (String block : blocks) {
                String trimmed = block.trim();
                if (trimmed.isEmpty()) continue;
                knowledgeService.addDocument(trimmed, Map.of("source", originalFilename));
                count++;
            }
            return Result.success("成功添加 " + count + " 个知识块");
        } catch (IOException e) {
            return Result.error(500, "文件读取失败");
        }
    }
}