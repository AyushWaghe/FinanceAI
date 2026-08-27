package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public org.springframework.ai.chat.client.ChatClient chatClient(org.springframework.ai.chat.client.ChatClient.Builder builder){
        return builder.build();
    }
}
