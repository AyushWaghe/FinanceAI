package org.example.dto;

import lombok.Data;

import java.util.Map;

@Data
public class LLMDocIngestionResponse {
    private boolean verified;
    private String reason;
    private String documentType;
    private String chunkingStrategy;
    private String docSummary;
}
