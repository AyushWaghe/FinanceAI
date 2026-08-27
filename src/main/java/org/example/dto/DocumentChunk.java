package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentChunk {
    private int chunkNumber;
    private String chunkContent;
}
