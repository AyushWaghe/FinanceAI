package org.example.AIEvals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.example.dto.ChunkingTestCase;
import org.example.dto.LLMDocIngestionResponse;
import org.example.prompts.PromptLoaderImpl;
import org.example.service.DocAnalyzerService;
import org.example.service.DocParserService;
import org.example.service.DocumentIngestionPipeline;
import org.example.service.LLMService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class IngestionEvalTest {

    @Autowired
    private LLMService llmService;

    @Autowired
    private DocParserService docParserService;

    @Autowired
    private DocAnalyzerService docAnalyzerService;

    @Autowired
    private PromptLoaderImpl promptLoader;

    //TEST METHODS==============================================================================================================

    @ParameterizedTest
    @MethodSource("chunkingTestCases")
    void testChunkingStrategy(String filePath, String expectedStrategy) throws IOException {

        InputStream inputStream = Files.newInputStream(Paths.get(filePath));

        String userPrompt = """
                    Below are the existing types of document user already has:
                    ""

                    Document is attached to you.
                    """;

        LLMDocIngestionResponse response =
                docAnalyzerService.analyzeDoc(userPrompt,promptLoader.load("LLMDocIngestionPrompt"),inputStream,filePath);

            assertEquals(expectedStrategy, response.getChunkingStrategy());

    }




    //HELPER METHODS================================================================================================

    static Stream<Arguments> chunkingTestCases() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<ChunkingTestCase> testCases = mapper.readValue(
                new File("D:\\NCDC\\FinanceAI\\src\\test\\resources\\test_cases\\chunk_strategy_detection_test_cases.json"),
                new TypeReference<List<ChunkingTestCase>>() {}
        );

        return testCases.stream()
                .map(testCase -> Arguments.of(
                        testCase.getFilePath(),
                        testCase.getExpectedChunkingStrategy()
                ));
    }
}
