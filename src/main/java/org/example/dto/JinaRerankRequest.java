package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JinaRerankRequest {
    private String model;
    private String query;
    private List<String> documents;
    private Integer top_n;
    private boolean return_documents=false;
}
