package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.DocumentChunk;
import org.example.dto.LLMDocIngestionResponse;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChunkingService {

    private final DocParserService docParserService;

    public List<String> chunk(
            String documentText,
            LLMDocIngestionResponse response,
            InputStream inputStream) {

        return switch (response.getChunkingStrategy()) {

            case "DOCUMENT" -> documentChunk(documentText);

            case "PAGE" -> pageChunk(inputStream);

            case "FIXED_SIZE"->fixedSizeChunk(documentText,response);

            default -> documentChunk(documentText);
        };
    }

    private List<String> documentChunk(String text) {

        return List.of(text);
    }

    private List<String> pageChunk(InputStream inputStream) {
        return docParserService.extractPDFPages(inputStream);
    }

    private List<String> fixedSizeChunk(
            String text,
            LLMDocIngestionResponse llmDocIngestionResponse
            ) {

        Integer chunkSize=llmDocIngestionResponse.getChunkSize();
        Integer overlap=llmDocIngestionResponse.getOverlap();

        if (text == null || text.isBlank()) {
            return List.of();
        }

        if (chunkSize == null || chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "Chunk size must be greater than 0"
            );
        }

        if (overlap == null || overlap < 0) {
            throw new IllegalArgumentException(
                    "Chunk overlap cannot be negative"
            );
        }

        if (overlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "Chunk overlap must be smaller than chunk size"
            );
        }

        List<String> chunks = new ArrayList<>();

        int start = 0;
        int textLength = text.length();

        while (start < textLength) {

            int end = Math.min(
                    start + chunkSize,
                    textLength
            );

            chunks.add(
                    text.substring(start, end).trim()
            );

            if (end == textLength) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }

}