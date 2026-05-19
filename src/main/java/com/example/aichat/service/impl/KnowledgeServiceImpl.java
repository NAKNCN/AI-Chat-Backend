package com.example.aichat.service.impl;

import com.example.aichat.service.KnowledgeService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private final VectorStore vectorStore;
    private final Map<String, Document> documentMap = new ConcurrentHashMap<>();

    public KnowledgeServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        loadFromFiles();
    }

    private void loadFromFiles() {
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:knowledge/*.txt");
            List<Document> docs = new ArrayList<>();

            for (Resource res : resources) {
                String content = res.getContentAsString(StandardCharsets.UTF_8).trim();
                if (content.isEmpty()) continue;

                String[] paragraphs = content.split("\n\n");
                String fileName = res.getFilename();

                for (int i = 0; i < paragraphs.length; i++) {
                    String paragraph = paragraphs[i].trim();
                    if (paragraph.isEmpty()) continue;

                    Document doc = new Document(paragraph);
                    doc.getMetadata().put("source", fileName);
                    doc.getMetadata().put("paragraph", i);
                    docs.add(doc);
                }
            }

            if (!docs.isEmpty()) {
                addDocuments(docs);
                System.out.println("✅ 知识库已加载，共 " + docs.size() + " 个文档段");
            }
        } catch (Exception e) {
            System.err.println("❌ 知识库加载失败（应用将继续启动）: " + e.getMessage());
        }
    }

    @Override
    public void addDocument(String content, Map<String, Object> metadata) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("文档内容不能为空");
        }
        String id = UUID.randomUUID().toString();
        Document doc = new Document(content);
        doc.getMetadata().putAll(metadata);
        doc.getMetadata().put("id", id);
        vectorStore.add(List.of(doc));
        documentMap.put(id, doc);
    }

    @Override
    public void addDocuments(List<Document> documents) {
        for (Document doc : documents) {
            String id = UUID.randomUUID().toString();
            doc.getMetadata().put("id", id);
            documentMap.put(id, doc);
        }
        vectorStore.add(documents);
    }

    @Override
    public boolean deleteDocument(String id) {
        Document removed = documentMap.remove(id);
        if (removed != null) {
            vectorStore.delete(List.of(id));
            return true;
        }
        return false;
    }

    @Override
    public List<Document> listDocuments() {
        return new ArrayList<>(documentMap.values());
    }
}