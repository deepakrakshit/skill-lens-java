package com.skilllens.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilllens.service.security.CredentialManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Resilient REST client for Google Gemini (Primary AI & Vision AI).
 * Default model: gemini-3.5-flash
 */
@Service
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String DEFAULT_MODEL = "gemini-3.5-flash";
    private static final String GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CredentialManager credentialManager;

    private String configuredModel = DEFAULT_MODEL;

    public GeminiClient(CredentialManager credentialManager, ObjectMapper objectMapper) {
        this.credentialManager = credentialManager;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String generateContent(String systemInstruction, String promptText) throws IOException, InterruptedException {
        return generateContentWithModel(this.configuredModel, systemInstruction, promptText);
    }

    public String generateContentWithModel(String model, String systemInstruction, String promptText) throws IOException, InterruptedException {
        Optional<String> apiKeyOpt = credentialManager.getApiKey(CredentialManager.KEY_GEMINI);
        if (apiKeyOpt.isEmpty() || apiKeyOpt.get().trim().isEmpty()) {
            throw new IllegalStateException("Google Gemini API key not configured. Set GEMINI_API_KEY in environment or Settings.");
        }

        String apiKey = apiKeyOpt.get().trim();
        String url = GEMINI_API_ENDPOINT + model + ":generateContent?key=" + apiKey;

        String combinedText = systemInstruction != null && !systemInstruction.trim().isEmpty()
                ? systemInstruction + "\n\n" + promptText
                : promptText;

        Map<String, Object> payload = Map.of(
                "contents", java.util.List.of(
                        Map.of("parts", java.util.List.of(
                                Map.of("text", combinedText)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json"
                )
        );

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        long startTime = System.currentTimeMillis();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long latency = System.currentTimeMillis() - startTime;

        int statusCode = response.statusCode();
        log.debug("Gemini response ({} ms, status {}): {}", latency, statusCode, response.body());

        if (statusCode >= 200 && statusCode < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            throw new IOException("Gemini returned empty candidate parts: " + response.body());
        }

        throw new IOException("Gemini API error (HTTP " + statusCode + "): " + response.body());
    }

    public String getConfiguredModel() {
        return configuredModel;
    }

    public void setConfiguredModel(String model) {
        if (model != null && !model.trim().isEmpty()) {
            this.configuredModel = model.trim();
        }
    }
}
