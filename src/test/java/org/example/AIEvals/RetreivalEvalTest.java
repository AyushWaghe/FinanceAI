package org.example.AIEvals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.grpc.Points;
import org.example.dto.LLMDocIngestionResponse;
import org.example.dto.LuceneRetrievedDoc;
import org.example.dto.RAGAgentTestCases;
import org.example.dto.VectorRetrievalTestCase;
import org.example.service.DocumentRetrievalPipeline;
import org.example.service.LLMService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RetreivalEvalTest {

    @Autowired
    private DocumentRetrievalPipeline documentRetrievalPipeline;

    @Autowired
    private LLMService llmService;

    @ParameterizedTest
    @MethodSource("vectorRetrievalTestData")
    void testVectorDBRetrievedChunks(
            String query,
            Integer userId,
            Set<String> expectedChunkIds) {

        List<Points.ScoredPoint> retrievedChunks =
                documentRetrievalPipeline.retrieveVector(query, userId);

        assertNotNull(retrievedChunks);

        List<String> retrievedChunkIds = retrievedChunks.stream()
                .map(point -> point.getId().getUuid())
                .toList();

        long relevantRetrieved = retrievedChunkIds.stream()
                .filter(expectedChunkIds::contains)
                .distinct()
                .count();

        double recall = expectedChunkIds.isEmpty()
                ? 0.0
                : (double) relevantRetrieved / expectedChunkIds.size();

        double precision = retrievedChunkIds.isEmpty()
                ? 0.0
                : (double) relevantRetrieved / retrievedChunkIds.size();

        Set<String> matchedChunkIds = retrievedChunkIds.stream()
                .filter(expectedChunkIds::contains)
                .collect(Collectors.toSet());

        System.out.println("""
            
            ========================================
            VECTOR RETRIEVAL EVALUATION
            ========================================
            Query:
            %s

            Expected Chunk IDs:
            %s

            Retrieved Chunk IDs:
            %s

            Matched Chunk IDs:
            %s

            Retrieved Count: %d
            Relevant Retrieved: %d
            Expected Relevant: %d

            Recall@10: %f
            Precision@10: %f
            ========================================
            """.formatted(
                query,
                expectedChunkIds,
                retrievedChunkIds,
                matchedChunkIds,
                retrievedChunkIds.size(),
                relevantRetrieved,
                expectedChunkIds.size(),
                recall,
                precision
        ));

        assertTrue(
                recall >= 0.8,
                "Recall below threshold: " + recall
        );
    }

    @ParameterizedTest
    @MethodSource("bm25RetrievalTestData")
    void textBM25RetrievedChunks(
            String query,
            Integer userId,
            Set<String> expectedChunkIds) {

        List<LuceneRetrievedDoc> retrievedDocs =
                documentRetrievalPipeline.retrieveBM25(query, userId);

        assertNotNull(retrievedDocs);

        long relevantRetrieved = retrievedDocs.stream()
                .map(LuceneRetrievedDoc::getChunkId)
                .filter(expectedChunkIds::contains)
                .distinct()
                .count();

        // Recall@10
        double recall = expectedChunkIds.isEmpty()
                ? 0.0
                : (double) relevantRetrieved / expectedChunkIds.size();

        // Precision@10
        double precision = retrievedDocs.isEmpty()
                ? 0.0
                : (double) relevantRetrieved / retrievedDocs.size();

        System.out.printf(
                """
                Query: %s
                Retrieved: %d
                Relevant Retrieved: %d
                Expected Relevant: %d
                Recall@10: %.3f
                Precision@10: %.3f
                %n
                """,
                query,
                retrievedDocs.size(),
                relevantRetrieved,
                expectedChunkIds.size(),
                recall,
                precision
        );

        assertTrue(
                recall >= 0.8,
                "BM25 Recall@10 below threshold: " + recall
        );
    }

    @ParameterizedTest
    @MethodSource("rerankerTestData")
    void testRerankerNDCG(
            String query,
            List<String> candidateChunks,
            Map<String, Integer> groundTruth) {

        List<String> rankedChunks =
                documentRetrievalPipeline.rerank(query, candidateChunks);

        assertNotNull(rankedChunks);

        int k = Math.min(5, rankedChunks.size());

        double dcg = 0.0;

        for (int i = 0; i < k; i++) {

            String chunk = rankedChunks.get(i);

            int relevance =
                    groundTruth.getOrDefault(chunk, 0);

            dcg += (Math.pow(2, relevance) - 1)
                    / (Math.log(i + 2) / Math.log(2));
        }

        List<Integer> idealRelevances =
                groundTruth.values()
                        .stream()
                        .sorted(Comparator.reverseOrder())
                        .limit(k)
                        .toList();

        double idcg = 0.0;

        for (int i = 0; i < idealRelevances.size(); i++) {

            int relevance = idealRelevances.get(i);

            idcg += (Math.pow(2, relevance) - 1)
                    / (Math.log(i + 2) / Math.log(2));
        }

        double ndcg = idcg == 0.0
                ? 0.0
                : dcg / idcg;

        System.out.printf(
                """
                Query: %s
                NDCG@5: %.3f
                """,
                query,
                ndcg
//                rankedChunks
        );

        assertTrue(
                ndcg >= 0.7,
                "NDCG@5 below threshold: " + ndcg
        );
    }

    @ParameterizedTest
    @MethodSource("ragTestData")
    void testRAGPipeline(
            String query,
            Integer userId,
            String expectedAnswer) {

        String actualAnswer =
                documentRetrievalPipeline.retrieveDocuments(query, userId);

        assertNotNull(actualAnswer);
        assertFalse(actualAnswer.isBlank());

        String evaluationPrompt = """
            Evaluate whether the actual answer correctly answers
            the user's question based on the expected answer.

            User Question:
            %s

            Expected Answer:
            %s

            Actual Answer:
            %s

            Return ONLY:
            PASS
            or
            FAIL

            PASS if the actual answer is factually consistent
            with the expected answer and correctly answers the question.
            FAIL if it contains incorrect, contradictory, or missing
            information that makes the answer materially incorrect.
            """.formatted(
                query,
                expectedAnswer,
                actualAnswer
        );

        String evaluation =
                llmService.ask(
                        "You are an evaluator for a RAG system.",
                        evaluationPrompt,
                        "GPT-4.1 mini"
                );

        assertEquals(
                "PASS",
                evaluation.trim(),
                """
                RAG evaluation failed.
                
                Query: %s
                Expected: %s
                Actual: %s
                Evaluator: %s
                """.formatted(
                        query,
                        expectedAnswer,
                        actualAnswer,
                        evaluation
                )
        );
    }

    //METHOD SRC ===================================================================================================
    static Stream<Arguments> vectorRetrievalTestData() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<VectorRetrievalTestCase> testCases = mapper.readValue(
                new File(
                        "D:\\NCDC\\FinanceAI\\src\\test\\resources\\test_cases\\vector_retrival_test_cases.json"
                ),
                new TypeReference<List<VectorRetrievalTestCase>>() {}
        );

        return testCases.stream()
                .map(testCase -> Arguments.of(
                        testCase.getQuery(),
                        testCase.getUserId(),
                        testCase.getExpectedChunkIds()
                ));
    }


    static Stream<Arguments> bm25RetrievalTestData() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<VectorRetrievalTestCase> testCases = mapper.readValue(
                new File(
                        "D:\\NCDC\\FinanceAI\\src\\test\\resources\\test_cases\\bm25_retrieval_test_cases.json"
                ),
                new TypeReference<List<VectorRetrievalTestCase>>() {}
        );

        return testCases.stream()
                .map(testCase -> Arguments.of(
                        testCase.getQuery(),
                        testCase.getUserId(),
                        testCase.getExpectedChunkIds()
                ));
    }

    static Stream<Arguments> ragTestData() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<RAGAgentTestCases> testCases = mapper.readValue(
                new File(
                        "D:\\NCDC\\FinanceAI\\src\\test\\resources\\test_cases\\rag_agent_test_cases.json"
                ),
                new TypeReference<List<RAGAgentTestCases>>() {}
        );

        return testCases.stream()
                .map(testCase -> Arguments.of(
                       testCase.getQuery(),
                        testCase.getUserId(),
                        testCase.getExpectedAnswer()
                ));
    }


}
