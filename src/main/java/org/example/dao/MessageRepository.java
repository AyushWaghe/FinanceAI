package org.example.dao;

import org.example.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository
        extends JpaRepository<Message, Long> {

    List<Message> findByConversationConversationIdOrderByCreatedAtAsc(Long conversationId);

    List<Message> findByConversationConversationIdAndCreatedAtGreaterThanOrderByCreatedAtAsc(
            Long conversationId,
            LocalDateTime timestamp
    );
}
