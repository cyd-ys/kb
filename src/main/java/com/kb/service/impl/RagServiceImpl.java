package com.kb.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kb.dto.QueryResponse;
import com.kb.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
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

        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String docId = UUID.randomUUID().toString();
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("file", out.getName());
            metadata.put("chunk_index", i);
            
            documents.add(new Document(docId, chunk, metadata));
        }

        vectorStore.add(documents);
        
        log.info("文件索引完成: {}，生成 {} 个知识块", filename, chunks.size());
        return "文件上传成功，已生成 " + chunks.size() + " 个知识块";
    }

    @Override
    public QueryResponse query(String question) {
        long start = System.currentTimeMillis();
        try {
            List<Document> results = vectorStore.similaritySearch(question);
            
            StringBuilder context = new StringBuilder();
            List<QueryResponse.SourceDocument> sources = new ArrayList<>();
            
            for (Document doc : results) {
                context.append(doc.getContent()).append("\n---\n");
                
                QueryResponse.SourceDocument sd = new QueryResponse.SourceDocument(
                    doc.getId(),
                    doc.getMetadata().get("file").toString(),
                    doc.getContent(),
                    doc.getEmbedding() != null ? doc.getEmbedding().similarity(question) : 0.0
                );
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
        stats.put("totalDocuments", new File(uploadPath).listFiles() == null ? 0 : new File(uploadPath).listFiles().length);
        stats.put("totalChunks", vectorStore.getDocumentCount());
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



    @Autowired
    private EmbeddingClient embeddingClient;

    private double[] getEmbedding(String text) {
        List<Double> embedding = embeddingClient.embed(text);
        double[] vec = new double[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vec[i] = embedding.get(i);
        }
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

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    private String callChatCompletion(String prompt) {
        String system = "You are an assistant that answers user questions based on provided context. Answer concisely and cite sources when appropriate.";
        
        // 提取context和question
        String context = "";
        String question = prompt;
        int ctxIdx = prompt.indexOf("Context:\n");
        if (ctxIdx >= 0) {
            int qIdx = prompt.indexOf("\nUser question:\n", ctxIdx);
            if (qIdx > ctxIdx) {
                context = prompt.substring(ctxIdx + "Context:\n".length(), qIdx);
                question = prompt.substring(qIdx + "\nUser question:\n".length());
            }
        }

        Prompt p = new Prompt(new SystemMessage(system), 
                            new UserMessage(question), 
                            new AssistantMessage(context));
        
        return chatClient.call(p).getResult().getOutput().getContent();
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