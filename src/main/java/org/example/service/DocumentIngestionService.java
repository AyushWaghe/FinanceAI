package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.prompts.PromptLoaderImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final DocParserService docParserService;
    private final PromptLoaderImpl promptLoader;
    private final LLMService llmService;

    public String ingestDocument(MultipartFile file,String fileDescription){
        // TODO
        // OCR
        // Prompt
        // Metadata
        // Chunking
        // Embedding
        // Qdrant

        String docText=docParserService.extractText(file);
        String systemPrompt=promptLoader.load("LLMDocIngestionPrompt");
        String userPrompt = """
        Below is description given by user about the document content:
        %s

        Below is the Document Content:
        %s
        """.formatted(fileDescription, docText);

        System.out.println("User prompt"+userPrompt);

        return llmService.ask(systemPrompt,userPrompt);

    }
}
