package com.skilllens.domain.repository;

import com.skilllens.domain.model.MentorshipSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, String> {
    List<MentorshipSession> findByCandidateIdOrderByUpdatedAtDesc(String candidateId);
}
