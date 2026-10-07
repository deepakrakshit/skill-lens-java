package com.skilllens.domain.repository;

import com.skilllens.domain.model.CandidateDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateDocumentRepository extends JpaRepository<CandidateDocument, String> {
    List<CandidateDocument> findByCandidateId(String candidateId);
}
