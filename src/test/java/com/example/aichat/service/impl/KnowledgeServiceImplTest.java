package com.example.aichat.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeServiceImplTest {

    @Mock
    private VectorStore vectorStore;

    private KnowledgeServiceImpl knowledgeService;

    @BeforeEach
    void setUp() {
        // 用构造函数注入 Mock 的 VectorStore
        knowledgeService = new KnowledgeServiceImpl(vectorStore);
    }

    @Test
    void testAddDocument() {
        // 准备测试数据
        String content = "测试文档内容";
        Map<String, Object> metadata = Map.of("source", "test");

        // 执行
        knowledgeService.addDocument(content, metadata);

        // 验证：listDocuments 应该返回 1 条记录
        List<Document> docs = knowledgeService.listDocuments();
        assertEquals(1, docs.size());
        assertEquals(content, docs.get(0).getText());
        assertEquals("test", docs.get(0).getMetadata().get("source"));
        assertNotNull(docs.get(0).getMetadata().get("id"));  // ID 自动生成

        // 验证：vectorStore.add 被调用了一次
        verify(vectorStore, times(1)).add(any());
    }

    @Test
    void testDeleteDocument() {
        // 1. 先添加一个文档
        String content = "待删除的文档";
        Map<String, Object> metadata = Map.of("source", "test");
        knowledgeService.addDocument(content, metadata);

        // 2. 获取刚添加的文档 ID
        List<Document> docs = knowledgeService.listDocuments();
        assertEquals(1, docs.size());
        String docId = (String) docs.get(0).getMetadata().get("id");

        // 3. 执行删除
        boolean deleted = knowledgeService.deleteDocument(docId);
        assertTrue(deleted);

        // 4. 验证文档列表为空
        assertEquals(0, knowledgeService.listDocuments().size());

        // 5. 验证 vectorStore.delete 被调用了一次
        verify(vectorStore, times(1)).delete(any());
    }

    @Test
    void testDeleteNonExistentDocument() {
        // 删除一个不存在的 ID
        boolean deleted = knowledgeService.deleteDocument("non-existent-id");
        assertFalse(deleted);
        // vectorStore.delete 不应该被调用
        verify(vectorStore, never()).delete(any());
    }
}