package com.example.aichat.config;

import com.example.aichat.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ChromaVectorStoreConfig {

    @Value("${chroma.base-url}")
    private String chromaBaseUrl;

    @Value("${chroma.collection-name}")
    private String collectionName;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new ChromaVectorStore(chromaBaseUrl, embeddingModel, collectionName);
    }

    @Bean
    ApplicationRunner loadKnowledgeBase(VectorStore vectorStore) {
        return args -> {
            List<Document> docs = new ArrayList<>();  // 👈 在 try 外部定义
            try {
                PathMatchingResourcePatternResolver resolver =
                        new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources("classpath:knowledge/*.txt");

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
                    vectorStore.add(docs);
                    System.out.println("✅ Chroma 知识库已加载，共 " + docs.size() + " 个文档段");
                }
            } catch (Exception e) {
                System.err.println("❌ Chroma 知识库加载失败（应用将继续启动）: " + e.getMessage());
                // 加载失败时，知识库为空，但不会阻止启动
            }
        };
    }
}