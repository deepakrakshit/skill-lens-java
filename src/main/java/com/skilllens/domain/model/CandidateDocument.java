package com.skilllens.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "candidate_documents")
public class CandidateDocument {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "candidate_id", length = 36, nullable = false)
    private String candidateId;

    @Column(name = "document_type", length = 32, nullable = false)
    private String documentType; // PDF, DOCX, TXT, PASTED

    @Lob
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private long fileSize;

    @Column(name = "is_ocr_fallback")
    private boolean isOcrFallback;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CandidateDocument() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public CandidateDocument(String candidateId, String documentType, String rawText, String fileName, long fileSize) {
        this();
        this.candidateId = candidateId;
        this.documentType = documentType;
        this.rawText = rawText;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isOcrFallback() {
        return isOcrFallback;
    }

    public void setOcrFallback(boolean ocrFallback) {
        isOcrFallback = ocrFallback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
