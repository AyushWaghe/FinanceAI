package org.example.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.qdrant.client.QdrantException;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserDocRepository;
import org.example.dto.LLMDocIngestionResponse;
import org.example.dto.LuceneDocumentData;
import org.example.enums.DocumentState;
import org.example.exceptions.*;
import org.example.model.UserDocument;
import org.example.prompts.PromptLoaderImpl;
import org.example.util.CodeUtils;
import org.example.util.FileUtils;
import org.example.util.TokenUtil;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionPipeline {

    private final DocParserService docParserService;
    private final LuceneService luceneService;
    private final PromptLoaderImpl promptLoader;
    private final LLMService llmService;
    private final EmbeddingModel embeddingModel;
    private final ChunkingService chunkingService;
    private final QdrantService qdrantService;
    private final FileService fileService;
    private final DocAnalyzerService docAnalyzerService;
    private final UserDocRepository userDocRepository;
    private final MeterRegistry meterRegistry;
    private Counter documentProcessedCounter;
    private Counter documentFailedCounter;
    private Timer indestionTimer;
    private Timer llmProcessTimer;

    @PostConstruct()
    public void init(){
        documentProcessedCounter=meterRegistry.counter("documents.processed");
        documentFailedCounter=meterRegistry.counter("documents.failed");
        indestionTimer=meterRegistry.timer("document.ingestion.time");
        llmProcessTimer=meterRegistry.timer("document.llm.process.time");
    }


    public void ingestDocument(String objectkey,Integer userId) {
        log.info("Starting document ingestion. objectKey={}, userId={}", objectkey, userId);
        UserDocument userDocument = userDocRepository
                .findByObjectKey(objectkey)
                .orElseThrow(() -> new DocumentNotFoundException(objectkey));

        Timer.Sample ingestionStopwatch=Timer.start(meterRegistry);



        try {
            String fileName = Paths.get(objectkey).getFileName().toString();
            Set<String> userDocCategories;

            //Download doc from s3 storage
            InputStream inputStream;
            try {
                inputStream= fileService.download(objectkey);
            }catch (Exception e){
                userDocument.setDocumentState(DocumentState.SERVER_ERROR);
                log.error("Document ingestion failed. objectKey={}", objectkey, e);
                documentFailedCounter.increment();
                throw new S3serviceException("Unable to get document from S3 storage"+objectkey);
            }

            byte[] bytes = inputStream.readAllBytes();
            InputStream inputStream1 =
                    new ByteArrayInputStream(bytes);

            InputStream inputStream2 =
                    new ByteArrayInputStream(bytes);

            InputStream inputStream3 =
                    new ByteArrayInputStream(bytes);


            //Get doc text
            String docText=docParserService.extract(inputStream1,objectkey);

            if(TokenUtil.getTokenCount(docText) > 50_000) throw new TokenLimitExceddedException("Token limit exceeded for the uploaded document"+fileName);


            //Fetch user data from SQL
            userDocCategories=userDocRepository.findDistinctCategoriesByUserId(userId); //Cache possible
            String userPrompt;
            if ("pdf".equalsIgnoreCase(FileUtils.getFileExtension(objectkey))) {

                userPrompt = """
                    Below are the existing types of document user already has:
                    %s

                    Document is attached to you.
                    """.formatted(userDocCategories);
            } else {
                userPrompt = """
                    Below are the existing types of document user already has:
                    %s

                    Document content:
                    %s
                    """.formatted(
                        userDocCategories,
                        docText
                );
            }

            Timer.Sample llmStopwatch=Timer.start(meterRegistry);
            LLMDocIngestionResponse llmDocIngestionResponse=docAnalyzerService.analyzeDoc(userPrompt,promptLoader.load("LLMDocIngestionPrompt"),inputStream2,objectkey);
            llmStopwatch.stop(llmProcessTimer);

            System.out.println("LLM response"+llmDocIngestionResponse);

            log.info("Document classified as {}", llmDocIngestionResponse.getDocumentType());

            Map<String,Object> docMetaData=new HashMap<>();
            docMetaData.put("documentType",llmDocIngestionResponse.getDocumentType());
            docMetaData.put("userId",userId);
            docMetaData.put("uploadDate", LocalDate.now().toString());
            docMetaData.put("docId",objectkey);


            List<String> chunkedDocument=chunkingService.chunk(docText,llmDocIngestionResponse,inputStream3);
            EmbeddingResponse response;
            try {
                response=embeddingModel.call(
                        new EmbeddingRequest(
                                chunkedDocument,
                                null
                        )
                );
            }catch (Exception e){
                userDocument.setDocumentState(DocumentState.SERVER_ERROR);
                log.error("Document ingestion failed. objectKey={}", objectkey, e);
                documentFailedCounter.increment();
                throw new LLMEmbeddingException("Unable to get embeddings");
            }


            List<Embedding> embeddings = response.getResults();
            List<Points.PointStruct> points = new ArrayList<>();
            List<LuceneDocumentData> luceneDocuments = new ArrayList<>();

            for (int i = 0; i < embeddings.size(); i++) {

                float[] embeddingVector = embeddings.get(i).getOutput();

                String chunkId = UUID.randomUUID().toString();
                docMetaData.put("chunkText",chunkedDocument.get(i));

                Points.PointStruct point =
                        qdrantService.createPoint(
                                UUID.fromString(chunkId),
                                CodeUtils.toList(embeddingVector),
                                docMetaData
                        );

                points.add(point);

                luceneDocuments.add(
                        new LuceneDocumentData(chunkedDocument.get(i),userId.toString(),chunkId,objectkey)
                );
            }
            qdrantService.savePoints(points);
            luceneService.saveLuceneDocuments(luceneDocuments);

            userDocument.setDocType(llmDocIngestionResponse.getDocumentType());
            userDocument.setDocSummary(llmDocIngestionResponse.getDocSummary());
            userDocument.setDocumentState(DocumentState.COMPLETED);

            log.info("Document ingestion completed successfully. objectKey={}", objectkey);
            documentProcessedCounter.increment();

        } catch (QdrantException e){
            userDocument.setDocumentState(DocumentState.SERVER_ERROR);
            log.error("Document ingestion failed. objectKey={}", objectkey, e);
            documentFailedCounter.increment();
            throw new QdrantException(e.getMessage());
        }catch (TokenLimitExceddedException e){
            userDocument.setDocumentState(DocumentState.REJECTED);
            log.error("Document ingestion failed due to token limit exceeded. objectKey={}", objectkey, e);
            documentFailedCounter.increment();
            throw new RuntimeException(e);
        }
        catch (Exception e){
            userDocument.setDocumentState(DocumentState.SERVER_ERROR);
            log.error("Document ingestion failed. objectKey={}", objectkey, e);
            documentFailedCounter.increment();
            throw new RuntimeException(e);
        }
        finally {
            ingestionStopwatch.stop(indestionTimer);
            userDocRepository.save(userDocument);
        }
   }
}
