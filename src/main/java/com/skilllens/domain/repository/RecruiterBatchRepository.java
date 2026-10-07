package com.skilllens.domain.repository;

import com.skilllens.domain.model.RecruiterBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruiterBatchRepository extends JpaRepository<RecruiterBatch, String> {
}
