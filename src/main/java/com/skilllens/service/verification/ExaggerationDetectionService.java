package com.skilllens.service.verification;

import com.skilllens.domain.model.SkillVerificationRecord;
import com.skilllens.domain.model.VerificationInsight;
import com.skilllens.domain.model.VerificationStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects resume claims whose stated strength exceeds available public evidence.
 * Adheres strictly to objective, evidence-grounded language standards.
 */
@Service
public class ExaggerationDetectionService {

    private static final List<Pattern> SUPERLATIVE_PATTERNS = List.of(
            Pattern.compile("\\b(?:expert|mastery|deep expertise|specialist|authority|advanced proficiency)\\s+(?:in|with|of)?\\s+([A-Za-z0-9#+.]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b([A-Za-z0-9#+.]+)\\s+(?:expert|master|architect|specialist)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(?:production experience|enterprise scale)\\s+(?:in|with)\\s+([A-Za-z0-9#+.]+)", Pattern.CASE_INSENSITIVE)
    );

    public record ExaggerationFlag(
            String skill,
            String claimPhrase,
            String findingExplanation,
            String suggestedRemediation
    ) {}

    public List<ExaggerationFlag> detectExaggerations(
            String resumeText,
            List<SkillVerificationRecord> verificationRecords) {

        if (resumeText == null || resumeText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, SkillVerificationRecord> recordMap = new HashMap<>();
        for (SkillVerificationRecord r : verificationRecords) {
            recordMap.put(r.normalizedSkill().toLowerCase(), r);
        }

        List<ExaggerationFlag> flags = new ArrayList<>();
        Set<String> flaggedSkills = new HashSet<>();

        for (Pattern p : SUPERLATIVE_PATTERNS) {
            Matcher m = p.matcher(resumeText);
            while (m.find()) {
                String candidateSkill = m.group(1).trim();
                String lowerSkill = candidateSkill.toLowerCase();

                // Look up in record map
                SkillVerificationRecord record = recordMap.get(lowerSkill);
                if (record != null && !flaggedSkills.contains(lowerSkill)) {
                    if (record.status() == VerificationStatus.CLAIMED_UNVERIFIED ||
                        record.status() == VerificationStatus.WEAK_EVIDENCE) {

                        flaggedSkills.add(lowerSkill);
                        flags.add(new ExaggerationFlag(
                                record.normalizedSkill(),
                                m.group(0),
                                "Advanced " + record.normalizedSkill() + " proficiency claim is not strongly supported by the available public GitHub evidence.",
                                "Frame as 'Familiar with " + record.normalizedSkill() + "' or build a dedicated GitHub project demonstrating " + record.normalizedSkill() + " workflows."
                        ));
                    }
                }
            }
        }

        return flags;
    }
}
