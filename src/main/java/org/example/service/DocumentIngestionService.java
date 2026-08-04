package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import org.example.dto.LLMDocIngestionResponse;
import org.example.prompts.PromptLoaderImpl;
import org.example.util.FloatToListConverter;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final DocParserService docParserService;
    private final PromptLoaderImpl promptLoader;
    private final LLMService llmService;
    private final ObjectMapper objectMapper;
    private final EmbeddingModel embeddingModel;
    private final ChunkingService chunkingService;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final QdrantService qdrantService;
    private final QdrantClient qdrantClient;

    public String ingestDocument(MultipartFile file,String fileDescription) {
        String docText=docParserService.extractText(file);
        String systemPrompt=promptLoader.load("LLMDocIngestionPrompt");
        String userPrompt = """
        Below is description given by user about the document content:
        %s

        Below is the Document Content:
        %s
        """.formatted(fileDescription, docText);

        String jsonResponse=llmService.ask(systemPrompt,userPrompt);
        System.out.println("Json response   "+jsonResponse);

        LLMDocIngestionResponse llmDocIngestionResponse=null;
        try {
            llmDocIngestionResponse=objectMapper.readValue(jsonResponse,LLMDocIngestionResponse.class);
        }catch (JsonProcessingException e){
            System.out.println(e);
        }
        if(llmDocIngestionResponse.isVerified()==false) return jsonResponse;

        //Save SQL and DOC first

        Map<String,Object> docMetaData=new HashMap<>();
        docMetaData.put("documentType",llmDocIngestionResponse.getDocumentType());
        docMetaData.put("userId",1234);
        docMetaData.put("uploadDate", LocalDate.now().toString());
        docMetaData.put("docId",1234);


        List<String> chunkedDocument=chunkingService.chunk(docText,llmDocIngestionResponse);

        EmbeddingResponse response=embeddingModel.call(
                new EmbeddingRequest(
                        chunkedDocument,
                        null
                )
        );

        List<Embedding> embeddings = response.getResults();

        List<Points.PointStruct> points=new ArrayList<>();

        System.out.println("llmIngestion Respsone"+jsonResponse);

        for (int i=0;i<embeddings.size();i++){
            float[] embeddingVector=embeddings.get(i).getOutput();
            Points.PointStruct p= qdrantService.createPoint(UUID.randomUUID(), FloatToListConverter.toList(embeddingVector),docMetaData);
            points.add(p);
        }

        qdrantService.savePoints(points);
        return jsonResponse;

   }
}
