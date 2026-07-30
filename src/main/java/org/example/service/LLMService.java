package org.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LLMService {
    private final ChatModel chatModel;

    public String ask(String systemPrompt,String userPrompt){
        System.out.println("System prompt"+systemPrompt);
        Prompt prompt=new Prompt(
                List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userPrompt)
                )
        );

        ChatResponse chatResponse=chatModel.call(prompt);
        return chatResponse.getResult().getOutput().getText();
    }
}
