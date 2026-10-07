package com.skilllens.service.document;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Java-native document extraction service supporting PDF, DOCX, TXT, and pasted text.
 * Defends against zip bombs, oversized files, and encrypted files.
 * Detects scanned PDFs to signal OCR/vision fallback.
 */
@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    private static final long MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024; // 25 MB limit
    private static final int MIN_SCANNED_TEXT_LENGTH = 80;

    public record ParseResult(
            String text,
            String documentType,
            long fileSize,
            boolean isScannedImageFallback,
            String fileName
    ) {}

    public ParseResult parseFile(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found at path: " + filePath);
        }

        long size = Files.size(filePath);
        if (size > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 25MB (" + size + " bytes)");
        }

        String fileName = filePath.getFileName().toString();
        String lower = fileName.toLowerCase();

        byte[] bytes = Files.readAllBytes(filePath);

        if (lower.endsWith(".pdf")) {
            return parsePdf(bytes, fileName);
        } else if (lower.endsWith(".docx")) {
            return parseDocx(bytes, fileName);
        } else if (lower.endsWith(".txt")) {
            return parseTxt(bytes, fileName);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + fileName + ". Expected PDF, DOCX, or TXT.");
        }
    }

    public ParseResult parsePastedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Pasted resume text cannot be empty");
        }
        String sanitized = sanitizeText(text);
        return new ParseResult(sanitized, "PASTED", sanitized.getBytes(StandardCharsets.UTF_8).length, false, "pasted-resume.txt");
    }

    public ParseResult parsePdf(byte[] pdfBytes, String fileName) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("PDF file is password-protected or encrypted: " + fileName);
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);
            String sanitized = sanitizeText(rawText);

            boolean isScanned = sanitized.length() < MIN_SCANNED_TEXT_LENGTH;
            if (isScanned) {
                log.info("PDF '{}' yielded minimal text ({} chars). Flagging for OCR/Vision fallback.", fileName, sanitized.length());
            }

            return new ParseResult(sanitized, "PDF", pdfBytes.length, isScanned, fileName);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IOException("Failed to parse PDF document '" + fileName + "': " + e.getMessage(), e);
        }
    }

    public ParseResult parseDocx(byte[] docxBytes, String fileName) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(docxBytes);
             XWPFDocument document = new XWPFDocument(bis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String rawText = extractor.getText();
            String sanitized = sanitizeText(rawText);
            return new ParseResult(sanitized, "DOCX", docxBytes.length, false, fileName);
        } catch (Exception e) {
            throw new IOException("Failed to parse DOCX document '" + fileName + "': " + e.getMessage(), e);
        }
    }

    public ParseResult parseTxt(byte[] txtBytes, String fileName) {
        String rawText = new String(txtBytes, StandardCharsets.UTF_8);
        String sanitized = sanitizeText(rawText);
        return new ParseResult(sanitized, "TXT", txtBytes.length, false, fileName);
    }

    private String sanitizeText(String input) {
        if (input == null) return "";
        // Strip null bytes, control characters except standard tabs and newlines
        return input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", " ").trim();
    }
}
