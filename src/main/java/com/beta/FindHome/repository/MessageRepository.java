package com.beta.FindHome.repository;

import com.beta.FindHome.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Replaces findByConversationId() — Spring Data derives the query from the method name,
    // OrderBy createdAt matches the manual ORDER BY in the DAOImpl
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
