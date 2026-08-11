package org.example.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.example.exceptions.DocumentNotFoundException;
import org.example.exceptions.LLMEmbeddingException;
import org.example.exceptions.LLMResponseParseException;
import org.example.exceptions.S3serviceException;
import org.example.model.UserDocument;
import org.example.prompts.PromptLoaderImpl;
import org.example.util.FloatToListConverter;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionService {

    private final DocParserService docParserService;
    private final LuceneService luceneService;
    private final PromptLoaderImpl promptLoader;
    private final LLMService llmService;
    private final ObjectMapper objectMapper;
    private final EmbeddingModel embeddingModel;
    private final ChunkingService chunkingService;
    private final QdrantService qdrantService;
    private final FileStorageService fileStorageService;
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


    public String ingestDocument(String objectkey,Integer userId) {
        log.info("Starting document ingestion. objectKey={}, userId={}", objectkey, userId);
        UserDocument userDocument = userDocRepository
                .findByObjectKey(objectkey)
                .orElseThrow(() -> new DocumentNotFoundException(objectkey));

        Timer.Sample ingestionStopwatch=Timer.start(meterRegistry);

        try {
            Set<String> userDocCategories;

            //Download doc from s3 storage
            InputStream inputStream;
            try {
                inputStream=fileStorageService.download(objectkey);
            }catch (Exception e){
                userDocument.setDocumentState(DocumentState.SERVER_ERROR);
                log.error("Document ingestion failed. objectKey={}", objectkey, e);
                documentFailedCounter.increment();
                throw new S3serviceException("Unable to get document from S3 storage"+objectkey);
            }


            //Get doc text
            String docText=docParserService.extract(inputStream,objectkey);
            String systemPrompt=promptLoader.load("LLMDocIngestionPrompt");

            //Fetch user data from SQL
            userDocCategories=userDocRepository.findDistinctCategoriesByUserId(userId); //Cache possible



            String userPrompt = """
            Below are the existing types of document user already has-:
             %s
        
             Below is the Document Content:
             %s
            """.formatted(userDocCategories,docText);

            Timer.Sample llmStopwatch=Timer.start(meterRegistry);
            String jsonResponse=llmService.ask(systemPrompt,userPrompt);
            llmStopwatch.stop(llmProcessTimer);

            LLMDocIngestionResponse llmDocIngestionResponse=objectMapper.readValue(jsonResponse,LLMDocIngestionResponse.class);

            log.info("Document classified as {}", llmDocIngestionResponse.getDocumentType());

            Map<String,Object> docMetaData=new HashMap<>();
            docMetaData.put("documentType",llmDocIngestionResponse.getDocumentType());
            docMetaData.put("userId",userId);
            docMetaData.put("uploadDate", LocalDate.now().toString());
            docMetaData.put("docId",objectkey);


            List<String> chunkedDocument=chunkingService.chunk(docText,llmDocIngestionResponse);
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

                Points.PointStruct point =
                        qdrantService.createPoint(
                                UUID.fromString(chunkId),
                                FloatToListConverter.toList(embeddingVector),
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
            return jsonResponse;

        } catch (QdrantException e){
            userDocument.setDocumentState(DocumentState.SERVER_ERROR);
            log.error("Document ingestion failed. objectKey={}", objectkey, e);
            documentFailedCounter.increment();
            throw new QdrantException(e.getMessage());
        }
        catch (JsonParseException e){
            userDocument.setDocumentState(DocumentState.SERVER_ERROR);
            log.error("Document ingestion failed. objectKey={}", objectkey, e);
            documentFailedCounter.increment();
            throw new LLMResponseParseException("Unable to parse JSON from LLM json response to LLM DTO");
        }
        catch (Exception e){
            userDocument.setDocumentState(DocumentState.SERVER_ERROR);
            log.error("Document ingestion failed. objectKey={}", objectkey, e);
            documentFailedCounter.increment();
            throw new RuntimeException(e);
        }finally {
            ingestionStopwatch.stop(indestionTimer);
            userDocRepository.save(userDocument);
        }
   }
}
