package org.example.service;

import org.example.dto.ReRankerCandidate;
import org.example.dto.RerankResult;

import java.util.List;

public interface Reranker {
    List<String> rerank(
            String query,
            List<String> candidates,
            int topK
    );
}
