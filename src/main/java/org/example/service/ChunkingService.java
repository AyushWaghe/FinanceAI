package org.example.service;

import org.example.dto.DocumentChunk;
import org.example.dto.LLMDocIngestionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChunkingService {

    public List<String> chunk(
            String documentText,
            LLMDocIngestionResponse response) {

        return switch (response.getChunkingStrategy()) {

            case "DOCUMENT" -> documentChunk(documentText);

            case "PAGE" -> pageChunk(documentText);

            default -> documentChunk(documentText);
        };
    }

    private List<String> documentChunk(String text) {

        return List.of(text);
    }

    private List<String> pageChunk(String text) {

        // implement later

        return List.of(
               text
        );
    }

}