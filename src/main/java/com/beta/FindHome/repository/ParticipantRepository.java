// ParticipantRepository.java
package com.beta.FindHome.repository;

import com.beta.FindHome.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    // Underscore forces explicit navigation: conversation → id, user → id
    boolean existsByConversation_IdAndUser_Id(UUID conversationId, UUID userId);

    List<Participant> findByUser_Id(UUID userId);
}