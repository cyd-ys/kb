package com.kb.service;

import com.kb.dto.QueryResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * RAG服务接口
 */
public interface RagService {
    String uploadAndIndexDocument(MultipartFile file) throws Exception;
    QueryResponse query(String question);
    QueryResponse query(String question, int topK, double similarityThreshold);
    Map<String, Object> getStatistics();
    void deleteDocument(Long documentId);
    void updateDocumentIndex(Long documentId) throws Exception;
}