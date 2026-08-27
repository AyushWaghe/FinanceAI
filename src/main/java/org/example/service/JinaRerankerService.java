package org.example.service;

import com.microsoft.schemas.office.office.STInsetMode;
import lombok.RequiredArgsConstructor;
import org.example.dto.JinaRerankRequest;
import org.example.dto.JinaRerankResponse;
import org.example.dto.ReRankerCandidate;
import org.example.dto.RerankResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JinaRerankerService implements Reranker {

    private final RestClient.Builder restClientBuilder;

    @Value("${jina.api-key}")
    private String apiKey;

    @Value("${jina.url}")
    private String url;

    @Value("${jina.model}")
    private String model;

    @Override
    public List<String> rerank(
            String query,
            List<String> reRankerCandidatesChunkTexts,
            int topK
    ) {

        JinaRerankRequest request =
                new JinaRerankRequest(
                        model,
                        query,
                        reRankerCandidatesChunkTexts,
                        topK,
                        false
                );

        JinaRerankResponse response =
                restClientBuilder
                        .build()
                        .post()
                        .uri(url)
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(JinaRerankResponse.class);

        return mapResponse(
                response,
                reRankerCandidatesChunkTexts
        );
    }

    private List<String> mapResponse(
            JinaRerankResponse response,
            List<String> candidates
    ) {

        List<String> results =
                new ArrayList<>();

        for (JinaRerankResponse.Result result
                : response.getResults()) {
            results.add(candidates.get(result.getIndex()));
        }

        return results;
    }
}