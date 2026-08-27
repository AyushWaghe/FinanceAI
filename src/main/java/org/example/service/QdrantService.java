package org.example.service;

import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.dto.RRFDocScore;
import org.example.dto.ReRankerCandidate;
import org.example.exceptions.QdrantServiceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.ConditionFactory.match;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

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

        createPayloadIndexes();

        System.out.println("Collection created successfully.");
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

    public List<Points.ScoredPoint> search(
            List<Float> queryVector,
            int userId,
            int limit
    ){

        try {
            Common.Filter filter= Common.Filter.newBuilder().addMust(match("userId", userId)).build();

            return qdrantClient
                    .searchAsync(
                            Points.SearchPoints.newBuilder()
                                    .setCollectionName(COLLECTION_NAME)
                                    .addAllVector(queryVector)
                                    .setFilter(filter)
                                    .setLimit(limit)
                                    .setWithPayload(enable(true))
                                    .build()
                    )
                    .get();
        }catch (Exception e){
            throw new QdrantException("Unable to fetch docs from qdrant"+e);
        }
    }

    public List<ReRankerCandidate> getChunksByIds(List<RRFDocScore> chunkIds) {

        if (chunkIds == null || chunkIds.isEmpty()) {
            return List.of();
        }

        List<Common.PointId> pointIds = chunkIds.stream()
                .map(id -> Common.PointId.newBuilder()
                        .setUuid(id.getChunkId())
                        .build())
                .toList();

        List<Points.RetrievedPoint> points;

        try {
            points = qdrantClient
                    .retrieveAsync(
                            COLLECTION_NAME,
                            pointIds,
                            true,   // with payload
                            false,  // don't retrieve vectors
                            null
                    )
                    .get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Qdrant retrieval interrupted", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Failed to retrieve chunks from Qdrant", e
            );
        }

        return points.stream()
                .map(point -> {

                    String chunkId =
                            point.getId().getUuid();

                    String docId =
                            point.getPayloadMap()
                                    .get("docId")
                                    .getStringValue();

                    String chunkText =
                            point.getPayloadMap()
                                    .get("chunkText")
                                    .getStringValue();

                    return new ReRankerCandidate(
                            chunkId,
                            docId,
                            chunkText
                    );
                })
                .toList();
    }

    public void deleteDocument(String docId) {

        try {

            Common.Match match = Common.Match.newBuilder()
                    .setKeyword(docId)
                    .build();

            Common.FieldCondition fieldCondition = Common.FieldCondition.newBuilder()
                    .setKey("docId")
                    .setMatch(match)
                    .build();

            Common.Condition condition = Common.Condition.newBuilder()
                    .setField(fieldCondition)
                    .build();

            Common.Filter filter = Common.Filter.newBuilder()
                    .addMust(condition)
                    .build();

            Points.PointsSelector selector =
                    Points.PointsSelector.newBuilder()
                            .setFilter(filter)
                            .build();

            qdrantClient
                    .deleteAsync(
                            Points.DeletePoints.newBuilder()
                                    .setCollectionName(COLLECTION_NAME)
                                    .setPoints(selector)
                                    .build()
                    )
                    .get();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to delete document from Qdrant: " + docId,
                    e
            );
        }
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

    public void createPayloadIndexes(){
        try {
            qdrantClient.createPayloadIndexAsync(
                    COLLECTION_NAME,
                    "userId",
                    Collections.PayloadSchemaType.Integer,
                    null,
                    true,
                    null,
                    null
            ).get();

            qdrantClient.createPayloadIndexAsync(
                    COLLECTION_NAME,
                    "docId",
                    Collections.PayloadSchemaType.Keyword,
                    null,
                    true,
                    null,
                    null
            ).get();
        }catch (Exception e){
            throw new QdrantServiceException("Qdrant service error-Unable to create payload indices due to"+e);
        }
    }
}
