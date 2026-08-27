package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LuceneRetrievedDoc {
    String docId;
    String chunkId;
    Integer rank;
    float BM25Score;
}
