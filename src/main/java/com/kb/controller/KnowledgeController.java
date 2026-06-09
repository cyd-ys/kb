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
            QueryResponse response = ragService.query(request.getQuestion());
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