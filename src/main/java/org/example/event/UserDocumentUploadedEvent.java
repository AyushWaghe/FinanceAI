package org.example.event;

public record UserDocumentUploadedEvent(
        Integer userId,
        String objectKey
) {
}
