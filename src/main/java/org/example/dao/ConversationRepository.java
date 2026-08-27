package org.example.dao;

import jakarta.transaction.Transactional;
import org.example.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUserId(Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Conversation c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    Optional<Conversation> findByUserId(Long userId);

}
