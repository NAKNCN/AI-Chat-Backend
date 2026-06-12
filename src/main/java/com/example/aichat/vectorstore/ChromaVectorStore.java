package com.example.aichat.vectorstore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ChromaVectorStore implements VectorStore {

    private final String chromaBaseUrl;
    private final EmbeddingModel embeddingModel;
    private final String collectionName;
    private final String collectionId;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private static final Logger log = LoggerFactory.getLogger(ChromaVectorStore.class);

    public ChromaVectorStore(String baseUrl, EmbeddingModel embeddingModel, String collectionName) {
        this.chromaBaseUrl = baseUrl;
        this.embeddingModel = embeddingModel;
        this.collectionName = collectionName;
        this.collectionId = ensureCollectionExists();
    }

    private String ensureCollectionExists() {
        // 检查是否已存在
        Request getReq = new Request.Builder()
                .url(chromaBaseUrl + "/api/v1/collections/" + collectionName)
                .get()
                .build();
        try (Response resp = okHttpClient.newCall(getReq).execute()) {
            if (resp.isSuccessful() && resp.body() != null) {
                String bodyStr = resp.body().string();
                Map<String, Object> map = objectMapper.readValue(bodyStr, new TypeReference<>() {
                });
                String id = (String) map.get("id");
                log.info("Chroma 集合已存在: {}, UUID: {}", collectionName, id);
                return id;
            }
        } catch (Exception ignored) {}

        // 创建集合
        String bodyJson = "{\"name\":\"" + collectionName + "\"}";
        RequestBody reqBody = RequestBody.create(bodyJson, MediaType.parse("application/json"));
        Request postReq = new Request.Builder()
                .url(chromaBaseUrl + "/api/v1/collections")
                .post(reqBody)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response resp = okHttpClient.newCall(postReq).execute()) {
            if (resp.isSuccessful() && resp.body() != null) {
                String bodyStr = resp.body().string();
                Map<String, Object> map = objectMapper.readValue(bodyStr, new TypeReference<>() {
                });
                String id = (String) map.get("id");
                log.info("Chroma 集合已创建: {}, UUID: {}", collectionName, id);
                return id;
            } else {
                throw new RuntimeException("创建集合失败: " + resp.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建 Chroma 集合", e);
        }
    }

    @Override
    public void add(List<Document> documents) {
        if (documents.isEmpty()) return;

        List<String> ids = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();

        for (Document doc : documents) {
            ids.add(UUID.randomUUID().toString());
            texts.add(doc.getText());
            metadatas.add(doc.getMetadata());
        }

        List<float[]> rawEmbeddings = embeddingModel.embed(texts);
        List<List<Float>> embeddingsList = new ArrayList<>();
        for (float[] arr : rawEmbeddings) {
            List<Float> list = new ArrayList<>(arr.length);
            for (float v : arr) list.add(v);
            embeddingsList.add(list);
        }

        // 手动构建 JSON
        StringBuilder json = new StringBuilder();
        json.append("{\"ids\":[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(ids.get(i)).append("\"");
        }
        json.append("],\"embeddings\":[");
        for (int i = 0; i < embeddingsList.size(); i++) {
            if (i > 0) json.append(",");
            List<Float> vec = embeddingsList.get(i);
            json.append("[");
            for (int j = 0; j < vec.size(); j++) {
                if (j > 0) json.append(",");
                json.append(String.format(Locale.US, "%.8f", vec.get(j)));
            }
            json.append("]");
        }
        json.append("],\"documents\":[");
        for (int i = 0; i < texts.size(); i++) {
            if (i > 0) json.append(",");
            String escaped = texts.get(i)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
            json.append("\"").append(escaped).append("\"");
        }
        json.append("],\"metadatas\":[");
        for (int i = 0; i < metadatas.size(); i++) {
            if (i > 0) json.append(",");
            try {
                json.append(objectMapper.writeValueAsString(metadatas.get(i)));
            } catch (Exception e) {
                json.append("{}");
            }
        }
        json.append("]}");

        String jsonStr = json.toString();
        // 替换为调试日志
        log.debug("OkHttp add 请求体 (前200字符): {}...", jsonStr.substring(0, Math.min(200, jsonStr.length())));

        RequestBody requestBody = RequestBody.create(jsonStr, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(chromaBaseUrl + "/api/v1/collections/" + collectionId + "/add")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "null";
                throw new RuntimeException("Chroma add 失败: " + response.code() + " " + errorBody);
            }
        } catch (IOException e) {
            throw new RuntimeException("Chroma add 网络异常", e);
        }
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        String query = request.getQuery();
        int topK = request.getTopK() > 0 ? request.getTopK() : 4;

        float[] queryEmbedding = embeddingModel.embed(query);
        List<Float> queryVector = new ArrayList<>(queryEmbedding.length);
        for (float v : queryEmbedding) queryVector.add(v);
        List<List<Float>> queryEmbeddings = List.of(queryVector);

        // 构建查询 JSON
        StringBuilder json = new StringBuilder("{\"query_embeddings\":[");
        for (List<Float> vec : queryEmbeddings) {
            json.append("[");
            for (int j = 0; j < vec.size(); j++) {
                if (j > 0) json.append(",");
                json.append(String.format(Locale.US, "%.8f", vec.get(j)));
            }
            json.append("]");
        }
        json.append("],\"n_results\":").append(topK).append("}");

        RequestBody requestBody = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request req = new Request.Builder()
                .url(chromaBaseUrl + "/api/v1/collections/" + collectionId + "/query")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Chroma query 失败: " + response.code());
            }
            String respStr = response.body() != null ? response.body().string() : "{}";
            Map<String, Object> respMap = objectMapper.readValue(respStr, new TypeReference<>() {
            });
            return parseQueryResult(respMap);
        } catch (IOException e) {
            throw new RuntimeException("Chroma query 网络异常", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Document> parseQueryResult(Map<String, Object> response) {
        List<Document> result = new ArrayList<>();
        List<List<String>> documentsList = (List<List<String>>) response.get("documents");
        List<List<Double>> distancesList = (List<List<Double>>) response.get("distances");
        List<List<Map<String, Object>>> metadatasList = (List<List<Map<String, Object>>>) response.get("metadatas");
        if (documentsList != null && !documentsList.isEmpty()) {
            List<String> docs = documentsList.get(0);
            List<Double> distances = distancesList != null ? distancesList.get(0) : null;
            List<Map<String, Object>> metas = metadatasList != null ? metadatasList.get(0) : null;
            for (int i = 0; i < docs.size(); i++) {
                Document doc = new Document(docs.get(i));
                if (metas != null && i < metas.size()) doc.getMetadata().putAll(metas.get(i));
                if (distances != null && i < distances.size()) doc.getMetadata().put("distance", distances.get(i));
                result.add(doc);
            }
        }
        return result;
    }

    @Override
    public Optional<Boolean> delete(List<String> ids) {
        if (ids.isEmpty()) return Optional.empty();
        String jsonStr = "{\"ids\":[" +
                String.join(",", ids.stream().map(id -> "\"" + id + "\"").toList()) + "]}";
        RequestBody requestBody = RequestBody.create(jsonStr, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(chromaBaseUrl + "/api/v1/collections/" + collectionId + "/delete")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 替换为警告日志
                log.warn("Chroma delete 返回非成功状态码: {}", response.code());
                return Optional.of(false);
            }
            return Optional.of(true);
        } catch (IOException e) {
            // 替换为错误日志
            log.error("Chroma delete 网络异常", e);
            return Optional.of(false);
        }
    }
}