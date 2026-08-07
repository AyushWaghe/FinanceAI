package org.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userDocumentUploadedTopic() {
        return TopicBuilder
                .name("user-document-uploaded-topic")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
