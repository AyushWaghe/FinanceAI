package org.example.dto;

import lombok.Data;

@Data
public class ChunkingTestCase {
    private String filePath;
    private String expectedChunkingStrategy;
}
