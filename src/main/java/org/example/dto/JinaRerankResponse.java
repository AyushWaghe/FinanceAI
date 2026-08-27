package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JinaRerankResponse {
    private String model;
    private Usage usage;
    private List<Result> results;

    @Data
    public static class Usage {
        private int total_tokens;
    }

    @Data
    public static class Result {
        private int index;
        private double relevance_score;
    }

}
