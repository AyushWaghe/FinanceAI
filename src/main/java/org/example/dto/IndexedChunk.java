package org.example.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class IndexedChunk {
    private UUID pointId;

    private String chunkText;

    private float[] embedding;

    private int chunkNumber;

    private LLMDocIngestionResponse metadata;
}
