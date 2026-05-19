package com.example.aichat.service;

import org.springframework.ai.document.Document;
import java.util.List;
import java.util.Map;

public interface KnowledgeService {
    void addDocument(String content, Map<String, Object> metadata);
    void addDocuments(List<Document> documents);
    boolean deleteDocument(String id);
    List<Document> listDocuments();
}