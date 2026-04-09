package com.beta.FindHome.repository;

import com.beta.FindHome.exception.ResourceNotFoundException;
import com.beta.FindHome.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    default Conversation findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + id));
    }

    @Query("""
            SELECT c FROM Conversation c
            JOIN c.participants p
            WHERE p.user.userName IN (:user1Name, :user2Name)
            GROUP BY c
            HAVING COUNT(DISTINCT p.user.userName) = 2
            """)
    List<Conversation> findConversationsBetweenUsers(
            @Param("user1Name") String user1Name,
            @Param("user2Name") String user2Name
    );

}