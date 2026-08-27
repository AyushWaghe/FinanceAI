package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.dto.LLMDocIngestionResponse;
import org.example.exceptions.LLMPromptException;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LLMService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public LLMDocIngestionResponse analyzeDocOrTxt(String systemPrompt, String userPrompt,String modelName) {
        try {
            Prompt prompt = new Prompt(
                    List.of(
                            new SystemMessage(systemPrompt),
                            new UserMessage(userPrompt)
                    ),
                    OpenAiChatOptions.builder()
                            .model(modelName)
                            .build()
            );

            ChatResponse chatResponse = chatModel.call(prompt);

            String responseText = chatResponse
                    .getResult()
                    .getOutput()
                    .getText();

            return objectMapper.readValue(
                    responseText,
                    LLMDocIngestionResponse.class
            );

        } catch (Exception e) {
            throw new LLMPromptException(
                    "Unable to prompt LLM Service due to"+e
            );
        }
    }

    public String ask(String systemPrompt, String userPrompt,String modelName) {
        try {
            Prompt prompt = new Prompt(
                    List.of(
                            new SystemMessage(systemPrompt),
                            new UserMessage(userPrompt)
                    ),
                    OpenAiChatOptions.builder()
                            .model(modelName)
                            .build()
            );

            ChatResponse chatResponse = chatModel.call(prompt);

            return chatResponse
                    .getResult()
                    .getOutput()
                    .getText();

        } catch (Exception e) {
            throw new LLMPromptException(
                    "Unable to prompt LLM Service due to "+e
            );
        }
    }

    public LLMDocIngestionResponse analyzePDF(
            String systemPrompt,
            String userPrompt,
            InputStream document,
            String filename,
            String modelName
    ) {

        try {
            Resource documentResource =
                    new InputStreamResource(document);

            Media pdfMedia = Media.builder()
                    .mimeType(
                            MimeTypeUtils.parseMimeType("application/pdf")
                    )
                    .data(documentResource)
                    .name(filename)
                    .build();

            UserMessage userMessage = UserMessage.builder()
                    .text(userPrompt)
                    .media(pdfMedia)
                    .build();

            Prompt prompt = new Prompt(
                    List.of(
                            new SystemMessage(systemPrompt),
                            userMessage
                    ),
                    OpenAiChatOptions.builder().model(modelName).build()
            );

            ChatResponse chatResponse =
                    chatModel.call(prompt);

            String responseText = chatResponse
                    .getResult()
                    .getOutput()
                    .getText();

            return objectMapper.readValue(
                    responseText,
                    LLMDocIngestionResponse.class
            );

        } catch (JsonProcessingException e) {

            throw new LLMPromptException(
                    "Unable to parse LLM document ingestion response due to "+e
            );

        } catch (Exception e) {

            throw new LLMPromptException(
                    "Unable to prompt LLM Service due to"+e
            );
        }
    }
}