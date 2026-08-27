package org.example.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RAGAgentTestCases {
    private String query;
    private Integer userId;
    private String expectedAnswer;
}
