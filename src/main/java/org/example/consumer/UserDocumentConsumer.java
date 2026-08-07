package org.example.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.UserDocumentUploadedEvent;
import org.example.service.DocumentIngestionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDocumentConsumer {

    private final DocumentIngestionService documentIngestionService;

    @KafkaListener(
            topics = "user-document-uploaded-topic",
            groupId = "user-document-uploaded-group"
    )
    public void triggerDocumentIngestionPipeline(UserDocumentUploadedEvent userDocumentUploadedEvent){
        log.error("Document pipeline triggered for. objectKey={} userId={}", userDocumentUploadedEvent.objectKey(), userDocumentUploadedEvent.userId());
        documentIngestionService.ingestDocument(userDocumentUploadedEvent.objectKey(), userDocumentUploadedEvent.userId());
    }
}
