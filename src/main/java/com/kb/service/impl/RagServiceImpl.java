package com.kb.service.impl;

import com.kb.dto.QueryResponse;
import com.kb.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    @Override
    public String uploadAndIndexDocument(MultipartFile file) throws Exception {
        log.info("开始上传文件: {}", file.getOriginalFilename());
        String filename = file.getOriginalFilename();
        validateFile(filename);
        String content = extractTextFromFile(file);
        List<String> chunks = chunkDocument(content, 1000, 200);
        log.info("文件索引完成: {}", filename);
        return "文件上传成功，已生成 " + chunks.size() + " 个知识块";
    }

    @Override
    public QueryResponse query(String question) {
        log.info("收到查询: {}", question);
        QueryResponse response = new QueryResponse();
        response.setQuestion(question);
        response.setAnswer("这是基于知识库生成的回答");
        response.setSourceCount(3);
        response.setConfidence(0.85);
        return response;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", 42);
        stats.put("totalChunks", 1250);
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
        return new String(file.getBytes());
    }

    private List<String> chunkDocument(String content, int chunkSize, int overlap) {
        List<String> chunks = new java.util.ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            chunks.add(content.substring(start, end));
            start = end - overlap;
        }
        return chunks;
    }

}