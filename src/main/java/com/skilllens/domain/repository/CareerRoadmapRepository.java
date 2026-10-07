package com.skilllens.domain.repository;

import com.skilllens.domain.model.CareerRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerRoadmapRepository extends JpaRepository<CareerRoadmap, String> {
    Optional<CareerRoadmap> findByCandidateId(String candidateId);
}
