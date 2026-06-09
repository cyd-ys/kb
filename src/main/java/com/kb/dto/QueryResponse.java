package com.kb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    private String question;
    private String answer;
    private Integer sourceCount;
    private Double confidence;
    private List<SourceDocument> sources;
    private Long responseTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDocument {
        private String documentId;
        private String title;
        private String content;
        private Double similarity;
    }
}