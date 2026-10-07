package com.skilllens.domain.repository;

import com.skilllens.domain.model.MentorshipMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorshipMessageRepository extends JpaRepository<MentorshipMessage, String> {
    List<MentorshipMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
