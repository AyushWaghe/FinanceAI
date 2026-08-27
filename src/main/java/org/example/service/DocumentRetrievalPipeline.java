package org.example.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.dto.LuceneRetrievedDoc;
import org.example.dto.RRFDocScore;
import org.example.dto.ReRankerCandidate;
import org.example.prompts.PromptLoaderImpl;
import org.example.util.CodeUtils;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DocumentRetrievalPipeline {
    private final LuceneService luceneService;
    private final EmbeddingModel embeddingModel;
    private final QdrantService qdrantService;
    private final JinaRerankerService jinaRerankerService;
    private final PromptLoaderImpl promptLoader;
    private final LLMService llmService;
    private final MeterRegistry meterRegistry;
    private Timer reRankerTimer;
    private Timer vectorDBRetrievalTimer;
    private Timer luceneTimer;
    private Timer llmProcessTimer;

    @PostConstruct()
    public void init(){
        reRankerTimer=meterRegistry.timer("document.rerank.time");
        vectorDBRetrievalTimer=meterRegistry.timer("document.vectorDB.time");
        luceneTimer=meterRegistry.timer("document.lucene.time");
        llmProcessTimer=meterRegistry.timer("document.llm.process.time");
    }

    public String retrieveDocuments(String query, Integer userId) {

        Timer.Sample llmResponseStopwatch=Timer.start(meterRegistry);
        List<String> rerankResults = retrieve(query, userId);

        String systemPrompt = promptLoader.load("RAGAgentPrompt");

        String userPrompt = """
            User query-:
            %s

            Below is the list of Document Content-:
            %s
            """.formatted(query, rerankResults);

        llmResponseStopwatch.stop(llmProcessTimer);
        return llmService.ask(systemPrompt, userPrompt,"gpt-4.1-mini");
    }


    public List<String> retrieve(String query, Integer userId) {



        CompletableFuture<List<LuceneRetrievedDoc>> luceneFuture =
                CompletableFuture.supplyAsync(
                        () -> retrieveBM25(query, userId)
                );

        CompletableFuture<List<Points.ScoredPoint>> qdrantFuture =
                CompletableFuture.supplyAsync(
                        () -> retrieveVector(query, userId)
                );

        List<LuceneRetrievedDoc> luceneRetrievedDocs =
                luceneFuture.join();

        List<Points.ScoredPoint> qdrantDoc =
                qdrantFuture.join();

        List<RRFDocScore> topCandidates =
                fuseWithRRF(
                        luceneRetrievedDocs,
                        qdrantDoc
                );

        List<ReRankerCandidate> reRankerCandidates =
                qdrantService.getChunksByIds(topCandidates);

        List<String> chunkTexts = reRankerCandidates.stream()
                .map(ReRankerCandidate::getChunkText)
                .toList();

        return rerank(
                query,
                chunkTexts
        );
    }

    //RETRIEVAL COMPOENENTS==================================================================================
    public List<LuceneRetrievedDoc> retrieveBM25(
            String query,
            Integer userId) {

        Timer.Sample luceneStopwatch=Timer.start(meterRegistry);
        List<LuceneRetrievedDoc> luceneRetrievedDocs=luceneService.searchLucene(
                query,
                userId.toString(),
                10
        );
        luceneStopwatch.stop(luceneTimer);
        return luceneRetrievedDocs;

    }

    public List<Points.ScoredPoint> retrieveVector(
            String query,
            Integer userId) {

        Timer.Sample vectorDBStopwatch=Timer.start(meterRegistry);

        EmbeddingResponse response =
                embeddingModel.call(
                        new EmbeddingRequest(
                                List.of(query),
                                null
                        )
                );

        float[] queryVec =
                response.getResult().getOutput();

        List<Float> queryVector =
                CodeUtils.toList(queryVec);

        vectorDBStopwatch.stop(vectorDBRetrievalTimer);

        return qdrantService.search(
                queryVector,
                userId,
                10
        );
    }

    public List<RRFDocScore> fuseWithRRF(
            List<LuceneRetrievedDoc> luceneRetrievedDocs,
            List<Points.ScoredPoint> qdrantDoc) {

        HashMap<String, RRFDocScore> map = new HashMap<>();

        int K = 60;

        // BM25
        for (LuceneRetrievedDoc doc : luceneRetrievedDocs) {

            String chunkId = doc.getChunkId();

            RRFDocScore result =
                    map.computeIfAbsent(
                            chunkId,
                            id -> new RRFDocScore(
                                    chunkId,
                                    doc.getDocId(),
                                    null,
                                    null,
                                    0
                            )
                    );

            result.setBM25Rank(doc.getRank());

            result.setRrfScore(
                    result.getRrfScore()
                            + (1.0 / (K + doc.getRank()))
            );
        }

        // Vector
        for (int i = 0; i < qdrantDoc.size(); i++) {

            Points.ScoredPoint point = qdrantDoc.get(i);

            String chunkId =
                    point.getId().getUuid();

            String docId =
                    point.getPayloadMap()
                            .get("docId")
                            .getStringValue();

            Integer vectorRank = i + 1;

            RRFDocScore result =
                    map.computeIfAbsent(
                            chunkId,
                            id -> new RRFDocScore(
                                    chunkId,
                                    docId,
                                    null,
                                    null,
                                    0
                            )
                    );

            result.setVectorRank(vectorRank);

            result.setRrfScore(
                    result.getRrfScore()
                            + (1.0 / (K + vectorRank))
            );
        }

        return map.values()
                .stream()
                .sorted(
                        Comparator.comparingDouble(
                                RRFDocScore::getRrfScore
                        ).reversed()
                )
                .limit(10)
                .toList();
    }

    public List<String> rerank(
            String query,
            List<String> reRankerCandidatesChunkTexts) {

        Timer.Sample rerankStopwatch=Timer.start(meterRegistry);

        List<String> rerankedDocs=jinaRerankerService.rerank(
                query,
                reRankerCandidatesChunkTexts,
                5
        );
        rerankStopwatch.stop(reRankerTimer);
        return rerankedDocs;
    }


}
