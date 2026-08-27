package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RRFDocScore {
    private String chunkId;
    private String docId;
    private Integer BM25Rank;
    private Integer vectorRank;
    private double rrfScore;
}
