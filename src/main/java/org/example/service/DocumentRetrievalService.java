package org.example.service;

import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import org.example.util.FloatToListConverter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentRetrievalService {
    private final LuceneService luceneService;
    private final EmbeddingModel embeddingModel;
    private final QdrantService qdrantService;

    public String retrieveDocuments(String query,Integer userId){
//        luceneService.searchLucene(query,userId.toString());
//        return "Success";
        EmbeddingResponse response=embeddingModel.call(
                new EmbeddingRequest(
                        List.of(query),
                        null
                )
        );

        float [] queryVec=response.getResult().getOutput();
        List<Float> queryVector= FloatToListConverter.toList(queryVec);
        List<Points.ScoredPoint> qdrantDoc=qdrantService.search(queryVector,userId,10);
        for (Points.ScoredPoint point : qdrantDoc) {

            System.out.println("------ Qdrant Result ------");

            System.out.println("ID: " + point.getId());
            System.out.println("Score: " + point.getScore());
            System.out.println("Payload: " + point.getPayloadMap());
        }
        return "";

    }
}
