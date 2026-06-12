package com.kb.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kb.dto.QueryResponse;
import com.kb.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    @Value("${kb.upload.path:uploads}")
    private String uploadPath;

    @Value("${kb.rag.chunk-size:1000}")
    private int chunkSize;

    @Value("${kb.rag.overlap:200}")
    private int overlap;

    @Value("${spring.ai.openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String OPENAI_EMBEDDING_MODEL = "text-embedding-3-small";

    private File indexFile() {
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "index.json");
    }

    @Override
    public String uploadAndIndexDocument(MultipartFile file) throws Exception {
        log.info("开始上传文件: {}", file.getOriginalFilename());
        String filename = file.getOriginalFilename();
        validateFile(filename);

        // save original file
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, UUID.randomUUID().toString() + "_" + filename);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(file.getBytes());
        }

        String content = extractTextFromFile(file);
        List<String> chunks = chunkDocument(content, chunkSize, overlap);

        // load existing index
        JSONArray index = loadIndex();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            double[] emb = getEmbedding(chunk);
            JSONObject item = new JSONObject();
            item.put("id", UUID.randomUUID().toString());
            item.put("file", out.getName());
            item.put("text", chunk);
            item.put("embedding", emb);
            index.add(item);
        }

        saveIndex(index);

        log.info("文件索引完成: {}，生成 {} 个知识块", filename, chunks.size());
        return "文件上传成功，已生成 " + chunks.size() + " 个知识块";
    }

    @Override
    public QueryResponse query(String question) {
        long start = System.currentTimeMillis();
        try {
            double[] qEmb = getEmbedding(question);

            JSONArray index = loadIndex();
            List<JSONObject> items = new ArrayList<>();
            for (int i = 0; i < index.size(); i++) items.add(index.getJSONObject(i));

            // compute similarities
            List<QueryResult> results = new ArrayList<>();
            for (JSONObject it : items) {
                double[] emb = it.getObject("embedding", double[].class);
                double score = cosineSimilarity(qEmb, emb);
                results.add(new QueryResult(it, score));
            }

            results.sort((a, b) -> Double.compare(b.score, a.score));

            int topK = 5;
            List<QueryResult> top = results.stream().limit(topK).collect(Collectors.toList());

            StringBuilder context = new StringBuilder();
            List<QueryResponse.SourceDocument> sources = new ArrayList<>();
            for (QueryResult r : top) {
                JSONObject it = r.item;
                context.append(it.getString("text")).append("\n---\n");
                QueryResponse.SourceDocument sd = new QueryResponse.SourceDocument(
                        it.getString("id"), it.getString("file"), it.getString("text"), r.score);
                sources.add(sd);
            }

            String prompt = buildPrompt(question, context.toString());
            String answer = callChatCompletion(prompt);

            QueryResponse resp = new QueryResponse();
            resp.setQuestion(question);
            resp.setAnswer(answer);
            resp.setSourceCount(sources.size());
            resp.setConfidence(null);
            resp.setSources(sources);
            resp.setResponseTime(System.currentTimeMillis() - start);
            return resp;
        } catch (Exception e) {
            log.error("查询失败", e);
            QueryResponse r = new QueryResponse();
            r.setQuestion(question);
            r.setAnswer("查询执行失败: " + e.getMessage());
            r.setResponseTime(System.currentTimeMillis() - start);
            return r;
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        JSONArray index = loadIndex();
        stats.put("totalDocuments", new File(uploadPath).listFiles() == null ? 0 : new File(uploadPath).listFiles().length);
        stats.put("totalChunks", index.size());
        stats.put("lastUpdated", System.currentTimeMillis());
        return stats;
    }

    @Override
    public void deleteDocument(Long documentId) {
        log.info("删除文档: {}", documentId);
    }

    @Override
    public void updateDocumentIndex(Long documentId) throws Exception {
        log.info("更新文档索引: {}", documentId);
    }

    private void validateFile(String filename) throws Exception {
        if (filename == null || filename.isEmpty()) {
            throw new Exception("文件名不能为空");
        }
    }

    private String extractTextFromFile(MultipartFile file) throws Exception {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private List<String> chunkDocument(String content, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            chunks.add(content.substring(start, end));
            start = Math.max(end - overlap, end);
        }
        return chunks;
    }

    private JSONArray loadIndex() {
        try {
            File idx = indexFile();
            if (!idx.exists()) return new JSONArray();
            String s = java.nio.file.Files.readString(idx.toPath(), StandardCharsets.UTF_8);
            return JSON.parseArray(s);
        } catch (Exception e) {
            log.warn("读取索引失败，返回空索引", e);
            return new JSONArray();
        }
    }

    private void saveIndex(JSONArray index) {
        try {
            File idx = indexFile();
            java.nio.file.Files.writeString(idx.toPath(), index.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("保存索引失败", e);
        }
    }

    private double[] getEmbedding(String text) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            // 回退：使用基于 SHA-256 的确定性局部向量（避免外部依赖，便于本地调试）
            return deterministicEmbedding(text, 1536);
        }

        JSONObject body = new JSONObject();
        body.put("model", OPENAI_EMBEDDING_MODEL);
        body.put("input", text);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/embeddings"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new Exception("Embedding 调用失败: " + resp.body());
        }

        JSONObject jo = JSON.parseObject(resp.body());
        JSONArray arr = jo.getJSONArray("data");
        if (arr == null || arr.isEmpty()) throw new Exception("Embedding 返回为空");
        JSONArray emb = arr.getJSONObject(0).getJSONArray("embedding");
        double[] vec = new double[emb.size()];
        for (int i = 0; i < emb.size(); i++) vec[i] = emb.getDouble(i);
        return vec;
    }

    private double[] deterministicEmbedding(String text, int dim) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            double[] vec = new double[dim];
            // expand hash to fill vector deterministically
            for (int i = 0; i < dim; i++) {
                int idx = i % hash.length;
                int unsigned = hash[idx] & 0xFF;
                vec[i] = (unsigned / 255.0) * (1.0 - ((i % 7) * 0.01));
            }
            // normalize
            double norm = 0;
            for (double v : vec) norm += v * v;
            norm = Math.sqrt(norm);
            if (norm == 0) return vec;
            for (int i = 0; i < vec.length; i++) vec[i] /= norm;
            return vec;
        } catch (Exception e) {
            // fallback simple vector
            double[] v = new double[dim];
            Arrays.fill(v, 0.0);
            return v;
        }
    }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null) return 0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private String buildPrompt(String question, String context) {
        String system = "You are an assistant that answers user questions based on provided context. Answer concisely and cite sources when appropriate.";
        String prompt = system + "\n\nContext:\n" + context + "\nUser question:\n" + question;
        return prompt;
    }

    private String callChatCompletion(String prompt) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            // 离线回退：从 prompt 中提取 context 并返回简短摘要，便于无网络/无密钥时本地演示
            try {
                String context = "";
                int ctxIdx = prompt.indexOf("Context:\n");
                if (ctxIdx >= 0) {
                    int qIdx = prompt.indexOf("\nUser question:\n", ctxIdx);
                    if (qIdx > ctxIdx) {
                        context = prompt.substring(ctxIdx + "Context:\n".length(), qIdx);
                    } else {
                        context = prompt.substring(ctxIdx + "Context:\n".length());
                    }
                } else {
                    // 如果无法解析，整段 prompt 作为 context
                    context = prompt;
                }
                String collapsed = context.replaceAll("\\s+", " ").trim();
                if (collapsed.length() > 600) collapsed = collapsed.substring(0, 600) + "...";
                String answer = "（离线回答）根据提供的上下文，摘要： " + collapsed;
                return answer;
            } catch (Exception e) {
                return "（离线回答）无法生成摘要";
            }
        }

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONObject body = new JSONObject();
        body.put("model", openaiModel != null ? openaiModel : "gpt-3.5-turbo");
        body.put("messages", new JSONArray().fluentAdd(message));
        body.put("max_tokens", 512);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new Exception("Chat API 调用失败: " + resp.body());
        }

        JSONObject jo = JSON.parseObject(resp.body());
        JSONArray choices = jo.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) return "";
        JSONObject first = choices.getJSONObject(0);
        JSONObject messageObj = first.getJSONObject("message");
        if (messageObj == null) return "";
        return messageObj.getString("content");
    }

    private static class QueryResult {
        JSONObject item;
        double score;

        QueryResult(JSONObject item, double score) {
            this.item = item;
            this.score = score;
        }
    }

}