package org.example.service;

import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.dto.IndexedChunk;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class QdrantService {
    private static final String COLLECTION_NAME = "finance_documents";
    private static final long VECTOR_SIZE = 1536;

    private final QdrantClient qdrantClient;

    @PostConstruct
    public void initializeCollection() throws ExecutionException, InterruptedException {
        boolean collectionsExists=qdrantClient.collectionExistsAsync(COLLECTION_NAME)
                .get();

        if (collectionsExists) return;

        System.out.println("Creating collection...");

        Collections.VectorParams vectorParams= Collections.VectorParams.newBuilder()
                .setSize(VECTOR_SIZE)
                .setDistance(Collections.Distance.Cosine)
                .build();

        qdrantClient.createCollectionAsync(
                COLLECTION_NAME,
                vectorParams
        ).get();

        System.out.println("Collection created successfully.");
    }

    public void storeDocuments(List<IndexedChunk> indexedChunks){
        //TO-DO
    }

    public void savePoints(List<Points.PointStruct> points){
        qdrantClient.upsertAsync(COLLECTION_NAME,points);
    }

    public Points.PointStruct createPoint(UUID id, List<Float> embedding, Map<String,Object> payload) {
        Points.PointStruct.Builder builder=Points.PointStruct.newBuilder(); //This creates an empty point
        builder.setId(PointIdFactory.id(id));
        builder.setVectors(
                VectorsFactory.vectors(embedding)
        );

        for (Map.Entry<String, Object> entry : payload.entrySet()) {

            builder.putPayload(
                    entry.getKey(),
                   getValue(entry.getValue())
            );
        }
        return builder.build();
    }

    public JsonWithInt.Value getValue(Object obj){
        if (obj instanceof String s) {
             return ValueFactory.value(s);
        } else if (obj instanceof Integer i) {
            return ValueFactory.value(i);
        } else if (obj instanceof Long l) {
            return ValueFactory.value(l);
        } else if (obj instanceof Float f) {
            return ValueFactory.value(f);
        } else if (obj instanceof Double d) {
            return ValueFactory.value(d);
        } else if (obj instanceof Boolean b) {
            return ValueFactory.value(b);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported type: " + obj.getClass().getName()
            );
        }
    }
}
