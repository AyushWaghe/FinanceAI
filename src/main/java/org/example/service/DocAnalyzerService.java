package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.LLMDocIngestionResponse;
import org.example.util.FileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocAnalyzerService {
    private final LLMService llmService;

    public LLMDocIngestionResponse analyzeDoc(String userPrompt,String systemPrompt,InputStream document, String objectKey){
        String extension= FileUtils.getFileExtension(objectKey);
        String fileName=FileUtils.getFileName(objectKey);

        try {

            return switch (extension) {

                case "pdf" ->
                        llmService.analyzePDF(systemPrompt,userPrompt,document,fileName,"gpt-4.1-mini");

                case "docx", "txt" ->
                        llmService.analyzeDocOrTxt(systemPrompt,userPrompt,"gpt-4.1-mini");

                default ->
                        throw new UnsupportedOperationException(
                                "Unsupported content type : " + extension
                        );
            };

        } catch (Exception e) {
            throw new RuntimeException("LLM failed to analyse uploaded document", e);
        }
    }


}
