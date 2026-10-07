---
name: skill-lens-document-intelligence
description: >-
  Directs robust Java-native document ingestion, extraction, and validation for resumes across PDF, DOCX, TXT,
  and plain-text formats using Apache Tika, PDFBox, and POI. Use when parsing uploaded resumes, handling malformed
  or encrypted files, extracting candidate sections, or isolating untrusted document inputs.
---

# SkillLens Document Intelligence

This skill establishes the parsing pipelines, failure-handling policies, and security controls for converting uploaded resumes into structured text while defending against malicious, malformed, or oversized inputs.

---

## 1. Supported Document Types & Extractors

The application supports student resume input across four primary formats:
1. **PDF (`.pdf`):** Standard vector text, multi-column layouts, embedded fonts.
2. **DOCX (`.docx`):** Microsoft Office Open XML documents, table cells, lists.
3. **Plain Text (`.txt`):** UTF-8 / ASCII structured text files.
4. **Pasted Text:** Direct user entry from clipboard in the UI.

### Primary Extraction Pipeline:
- **Primary Engine:** Apache Tika (`AutoDetectParser`) provides unified MIME detection, metadata extraction, and body text parsing.
- **Dedicated PDF Fallback:** Apache PDFBox (`PDFTextStripper`) with custom positional sorting (`setSortByPosition(true)`) to handle complex two-column academic and engineering resume layouts.
- **Dedicated DOCX Fallback:** Apache POI (`XWPFDocument` and `XWPFWordExtractor`) for table extraction and bulleted list parsing.

---

## 2. Failure Handling & Defensive Policies

Resumes uploaded by students frequently exhibit edge-case formatting issues:
- **Encrypted / Password-Protected Files:**
  - Catch `InvalidPasswordException` / `EncryptedDocumentException`.
  - Display informative UI error: *"The uploaded PDF is password-protected. Please upload an unprotected copy."* Never crash the application.
- **Empty or Zero-Byte Files:**
  - Enforce minimum file size check (> 100 bytes). If text yield is under 20 characters, flag as empty document.
- **Oversized Documents:**
  - Enforce a strict 15 MB file size limit before attempting extraction to protect memory and JVM heaps.
- **Scanned / Image-Only PDFs:**
  - Detect image-only documents when extracted character count is near zero despite valid PDF page streams.
  - Provide graceful notification informing the user that vector text or a text-based format is required for automated skill extraction, with OCR fallback where Tesseract native binaries are detected.
- **Malformed Streams:**
  - Wrap all stream operations in try-with-resources. Catch generic I/O exceptions, log diagnostic warnings, and return structured parsing errors to the caller.

---

## 3. UI Thread Protection & Concurrency

Document extraction and OCR are CPU-intensive operations that must NEVER run on the JavaFX UI thread:
- Execute extraction inside a background worker using `javafx.concurrent.Task` or Java 25 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`).
- Report progress percentage and current parsing step to the UI via observable property bindings.
- Dispatch UI updates through `Platform.runLater()` upon task completion or error.

---

## 4. Structured Section Segmentation

Extracted document text is normalized and segmented into canonical resume sections using deterministic regex headers:
- **Identity & Contact Details:** Full name, email, phone number, location, GitHub URL, LinkedIn URL, portfolio link.
- **Summary / Objective:** Career summary, professional statement.
- **Education:** Degree, major, institution, graduation year, GPA.
- **Experience:** Job title, organization, dates, bullet points.
- **Projects:** Project title, technologies used, descriptions, repository links.
- **Skills:** Categorized skills (languages, frameworks, databases, cloud tools).
- **Certifications & Achievements:** Official certifications, awards, competitive programming honors.

---

## 5. Security & Untrusted Input Isolation

All document text is treated as **untrusted data**:
- Strip non-printable ASCII control characters (preserving newlines, tabs, and carriage returns).
- Defend against prompt-injection attacks embedded within resume white-text or metadata.
- Sanitize all strings before rendering in UI components to prevent markup injection.
