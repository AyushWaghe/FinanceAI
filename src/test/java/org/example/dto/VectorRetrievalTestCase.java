package org.example.dto;

import lombok.Data;

import java.util.Set;

@Data
public class VectorRetrievalTestCase {

    private String query;
    private Integer userId;
    private Set<String> expectedChunkIds;
}