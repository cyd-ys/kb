package com.kb.controller;

import com.kb.dto.QueryRequest;
import com.kb.dto.QueryResponse;
import com.kb.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库问答控制器
 */
@Slf4j
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final RagService ragService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String result = ragService.uploadAndIndexDocument(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
        try {
            int topK = request.getTopK() != null ? request.getTopK() : 5;
            double threshold = request.getSimilarityThreshold() != null ? request.getSimilarityThreshold() : 0.7;
            QueryResponse response = ragService.query(request.getQuestion(), topK, threshold);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/query")
    public ResponseEntity<QueryResponse> queryGet(@RequestParam("q") String question,
                                                  @RequestParam(value = "topK", defaultValue = "5") int topK,
                                                  @RequestParam(value = "threshold", defaultValue = "0.7") double threshold) {
        try {
            QueryResponse response = ragService.query(question, topK, threshold);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/query/{question}")
    public ResponseEntity<QueryResponse> queryPath(@PathVariable("question") String question,
                                                   @RequestParam(value = "topK", defaultValue = "5") int topK,
                                                   @RequestParam(value = "threshold", defaultValue = "0.7") double threshold) {
        try {
            QueryResponse response = ragService.query(question, topK, threshold);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/query-get")
    public ResponseEntity<QueryResponse> queryGetSimple(@RequestParam("q") String question,
                                                         @RequestParam(value = "topK", defaultValue = "5") int topK,
                                                         @RequestParam(value = "threshold", defaultValue = "0.7") double threshold) {
        try {
            QueryResponse response = ragService.query(question, topK, threshold);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(ragService.getStatistics());
    }

}