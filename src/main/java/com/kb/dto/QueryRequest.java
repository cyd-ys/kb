package com.kb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {
    private String question;
    private Integer topK = 5;
    private Double similarityThreshold = 0.7;
}