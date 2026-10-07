package com.skilllens.controller;

import com.skilllens.domain.model.RecruiterBatch;
import com.skilllens.domain.model.RecruiterCandidateAssessment;
import com.skilllens.service.recruiter.RecruiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for recruiter batch operations and fairness evaluations.
 */
@RestController
@RequestMapping("/api/recruiter")
@CrossOrigin(origins = "*")
public class RecruiterApiController {

    private final RecruiterService recruiterService;

    public RecruiterApiController(RecruiterService recruiterService) {
        this.recruiterService = recruiterService;
    }

    public record CreateBatchRequest(
            String roleTitle,
            String jobDescription
    ) {}

    @PostMapping("/batch")
    public ResponseEntity<RecruiterBatch> createBatch(@RequestBody CreateBatchRequest request) {
        RecruiterBatch batch = recruiterService.createBatch(request.roleTitle(), request.jobDescription());
        return ResponseEntity.ok(batch);
    }

    @GetMapping("/batches")
    public ResponseEntity<List<RecruiterBatch>> getAllBatches() {
        return ResponseEntity.ok(recruiterService.getAllBatches());
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<RecruiterBatch> getBatch(@PathVariable String batchId) {
        return recruiterService.getBatch(batchId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch/{batchId}/candidates")
    public ResponseEntity<List<RecruiterCandidateAssessment>> getRankedCandidates(@PathVariable String batchId) {
        List<RecruiterCandidateAssessment> ranked = recruiterService.getRankedCandidatesForBatch(batchId);
        return ResponseEntity.ok(ranked);
    }

    @PostMapping("/batch/{batchId}/save")
    public ResponseEntity<Map<String, String>> saveAssessments(
            @PathVariable String batchId,
            @RequestBody List<RecruiterCandidateAssessment> assessments) {
        recruiterService.saveAssessmentsToBatch(batchId, assessments);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Assessments saved and ranked"));
    }
}
