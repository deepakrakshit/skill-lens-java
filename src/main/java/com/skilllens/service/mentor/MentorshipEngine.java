package com.skilllens.service.mentor;

import com.skilllens.domain.model.*;
import com.skilllens.domain.repository.MentorshipMessageRepository;
import com.skilllens.domain.repository.MentorshipSessionRepository;
import com.skilllens.service.ai.AiRoutingService;
import com.skilllens.service.rag.HybridSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Conversational Career Mentorship Engine.
 * Grounds student queries against retrieved resume claims, verified GitHub evidence,
 * target role requirements, and current roadmap objectives using local RAG.
 * Enforces strict grounding, non-fabrication, and contextual refusal policies.
 */
@Service
public class MentorshipEngine {

    private static final Logger log = LoggerFactory.getLogger(MentorshipEngine.class);

    private final HybridSearchService searchService;
    private final AiRoutingService aiRouter;
    private final MentorshipSessionRepository sessionRepository;
    private final MentorshipMessageRepository messageRepository;

    public MentorshipEngine(HybridSearchService searchService,
                            AiRoutingService aiRouter,
                            MentorshipSessionRepository sessionRepository,
                            MentorshipMessageRepository messageRepository) {
        this.searchService = searchService;
        this.aiRouter = aiRouter;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Answers a student's mentorship question grounded in their verified evidence.
     */
    @Transactional
    public String askMentor(String candidateId, String sessionId, String userQuestion,
                            String targetRole, RoleFitResult fitResult, CareerRoadmap roadmap) {
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            return "Please provide a question regarding your skill verification, career roadmap, or project development.";
        }

        long numericStudentId = candidateId != null ? (long) Math.abs(candidateId.hashCode()) : 1L;

        // 1. Retrieve relevant evidence chunks (top 6 chunks)
        List<KnowledgeChunk> retrievedChunks = searchService.search(userQuestion, numericStudentId, 6);

        // 2. Build Structured Citation Block
        StringBuilder evidenceBuilder = new StringBuilder();
        evidenceBuilder.append("### RETRIEVED EVIDENCE\n");
        if (retrievedChunks.isEmpty()) {
            evidenceBuilder.append("No specific evidence chunks directly matched the query.\n");
        } else {
            int docIdx = 1;
            for (KnowledgeChunk chunk : retrievedChunks) {
                evidenceBuilder.append(String.format("[Doc %d - %s (%s)]: %s\n",
                        docIdx++, chunk.sourceTitle(), chunk.category(), chunk.content()));
            }
        }

        // 3. Assemble Grounded System Instruction
        StringBuilder systemInstruction = new StringBuilder();
        systemInstruction.append("You are the SkillLens Technical Career Mentor, an expert engineering lead.\n");
        systemInstruction.append("Your role is to guide the student towards achieving readiness for the target role: ")
                .append(targetRole != null ? targetRole : "Software Engineer").append(".\n\n");

        if (fitResult != null) {
            systemInstruction.append(String.format("Current Role Fit Score: %.1f/100 (Required Coverage: %.1f%%, Verified Coverage: %.1f%%).\n",
                    fitResult.overallScore(), fitResult.requiredSkillCoverage(), fitResult.verifiedSkillCoverage()));
        }

        systemInstruction.append("\nMANDATORY GROUNDING & REFUSAL INVARIANTS:\n");
        systemInstruction.append("1. Answer the student's question relying strictly on the retrieved context and verified profile details.\n");
        systemInstruction.append("2. If the retrieved evidence does not contain sufficient facts to answer regarding a specific competency or project, explicitly state:\n");
        systemInstruction.append("   'Based on your currently available resume and GitHub evidence, there is no recorded project activity for this competency.'\n");
        systemInstruction.append("3. Never fabricate unrecorded skills, employers, or metrics.\n");
        systemInstruction.append("4. Conclude your answer with an actionable, concrete GitHub or repository coding action.\n\n");

        systemInstruction.append(evidenceBuilder);

        String defensiveInstruction = aiRouter.buildDefensiveSystemInstruction(systemInstruction.toString());

        // 4. Retrieve recent message history (last 5 messages)
        StringBuilder promptWithHistory = new StringBuilder();
        if (sessionId != null) {
            List<MentorshipMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            int start = Math.max(0, history.size() - 5);
            if (!history.isEmpty()) {
                promptWithHistory.append("Recent conversation context:\n");
                for (int i = start; i < history.size(); i++) {
                    MentorshipMessage msg = history.get(i);
                    promptWithHistory.append(msg.getSenderRole()).append(": ").append(msg.getContent()).append("\n");
                }
                promptWithHistory.append("\n");
            }
        }

        promptWithHistory.append("Student Question: ").append(aiRouter.wrapUntrustedContent(userQuestion));

        // 5. Execute with AI Router failover
        String mentorReply;
        try {
            AiExecutionResult result = aiRouter.executeWithFailover(defensiveInstruction, promptWithHistory.toString());
            mentorReply = result.content();
        } catch (Exception e) {
            log.warn("AI generation failed in MentorshipEngine: {}. Using deterministic fallback response.", e.getMessage());
            mentorReply = buildDeterministicFallbackReply(userQuestion, targetRole, fitResult, retrievedChunks);
        }

        // 6. Record conversation in repository if session exists
        if (sessionId != null && sessionRepository.existsById(sessionId)) {
            MentorshipSession session = sessionRepository.findById(sessionId).orElse(null);
            if (session != null) {
                MentorshipMessage userMsg = new MentorshipMessage(session.getId(), "STUDENT", userQuestion);
                MentorshipMessage assistantMsg = new MentorshipMessage(session.getId(), "MENTOR", mentorReply);
                messageRepository.save(userMsg);
                messageRepository.save(assistantMsg);
            }
        }

        return mentorReply;
    }

    private String buildDeterministicFallbackReply(String query, String targetRole, RoleFitResult fitResult, List<KnowledgeChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        sb.append("Based on your analyzed SkillLens profile for the ").append(targetRole).append(" role:\n\n");
        if (fitResult != null) {
            sb.append(String.format("- Overall Role Fit: %.1f/100\n", fitResult.overallScore()));
            sb.append("- Verified Skills Count: ").append(fitResult.verifiedSkills().size()).append("\n");
            sb.append("- Identified Skill Gaps Count: ").append(fitResult.missingSkills().size()).append("\n\n");
        }
        if (!chunks.isEmpty()) {
            sb.append("Key matching evidence from your repositories:\n");
            for (KnowledgeChunk chunk : chunks) {
                sb.append("• ").append(chunk.sourceTitle()).append(": ").append(chunk.content()).append("\n");
            }
            sb.append("\n");
        }
        sb.append("Next Concrete Action: Review your highest priority skill gap in your roadmap and create a new feature pull request with comprehensive test coverage demonstrating this capability.");
        return sb.toString();
    }
}
