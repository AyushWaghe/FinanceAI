package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReRankerCandidate {
    private String chunkId;
    private String docId;
    private String chunkText;
}
