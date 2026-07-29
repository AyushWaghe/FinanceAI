package org.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;

    public String chat(String message){

        Prompt prompt=new Prompt(
                new UserMessage(message)
        );

        ChatResponse response=chatModel.call(prompt);

        System.out.println(response);

        return response.getResult().getOutput().getText();
    }
}
