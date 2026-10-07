package com.skilllens.service.recruiter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.domain.model.RecruiterBatch;
import com.skilllens.domain.model.RecruiterCandidateAssessment;
import com.skilllens.domain.repository.RecruiterBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Service for managing recruiter candidate workspaces and evaluation batches.
 */
@Service
public class RecruiterService {

    private static final Logger log = LoggerFactory.getLogger(RecruiterService.class);

    private final RecruiterBatchRepository batchRepository;
    private final CandidateRankingService rankingService;
    private final ObjectMapper objectMapper;

    public RecruiterService(RecruiterBatchRepository batchRepository,
                            CandidateRankingService rankingService,
                            ObjectMapper objectMapper) {
        this.batchRepository = batchRepository;
        this.rankingService = rankingService;
        this.objectMapper = objectMapper;
    }

    public RecruiterBatch createBatch(String roleTitle, String jobDescription) {
        RecruiterBatch batch = new RecruiterBatch(roleTitle, jobDescription);
        batch.setCandidatesJson("[]");
        return batchRepository.save(batch);
    }

    public List<RecruiterBatch> getAllBatches() {
        return batchRepository.findAll();
    }

    public Optional<RecruiterBatch> getBatch(String batchId) {
        return batchRepository.findById(batchId);
    }

    public List<RecruiterCandidateAssessment> getRankedCandidatesForBatch(String batchId) {
        Optional<RecruiterBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            return Collections.emptyList();
        }

        RecruiterBatch batch = batchOpt.get();
        List<RecruiterCandidateAssessment> assessments = deserializeAssessments(batch.getCandidatesJson());
        return rankingService.rankCandidates(assessments);
    }

    public void saveAssessmentsToBatch(String batchId, List<RecruiterCandidateAssessment> assessments) {
        Optional<RecruiterBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isPresent()) {
            RecruiterBatch batch = batchOpt.get();
            List<RecruiterCandidateAssessment> ranked = rankingService.rankCandidates(assessments);
            try {
                batch.setCandidatesJson(objectMapper.writeValueAsString(ranked));
                batchRepository.save(batch);
            } catch (IOException e) {
                log.error("Failed to serialize batch assessments: {}", e.getMessage());
            }
        }
    }

    public List<RecruiterCandidateAssessment> deserializeAssessments(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<RecruiterCandidateAssessment>>() {});
        } catch (IOException e) {
            log.error("Failed to deserialize candidate assessments: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
