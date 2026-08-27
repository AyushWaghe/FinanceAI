package org.example.dto;

import lombok.Data;

import java.util.Map;

@Data
public class LLMDocIngestionResponse {
    private String documentType;
    private String chunkingStrategy;
    private String docSummary;
    private Integer chunkSize;
    private Integer overlap;
}
