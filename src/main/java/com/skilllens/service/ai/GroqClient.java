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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resilient REST client for Groq (Fallback AI).
 * Default model: openai/gpt-oss-120b
 * Uses OpenAI-compatible chat completions API.
 */
@Service
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    public static final String DEFAULT_MODEL = "openai/gpt-oss-120b";
    private static final String GROQ_API_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CredentialManager credentialManager;

    private String configuredModel = DEFAULT_MODEL;

    public GroqClient(CredentialManager credentialManager, ObjectMapper objectMapper) {
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
        Optional<String> apiKeyOpt = credentialManager.getApiKey(CredentialManager.KEY_GROQ);
        if (apiKeyOpt.isEmpty() || apiKeyOpt.get().trim().isEmpty()) {
            throw new IllegalStateException("Groq API key not configured. Set GROQ_API_KEY in environment or Settings.");
        }

        String apiKey = apiKeyOpt.get().trim();

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemInstruction != null && !systemInstruction.trim().isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemInstruction));
        }
        messages.add(Map.of("role", "user", "content", promptText));

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object")
        );

        String jsonBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_ENDPOINT))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        long startTime = System.currentTimeMillis();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long latency = System.currentTimeMillis() - startTime;

        int statusCode = response.statusCode();
        log.debug("Groq response ({} ms, status {}): {}", latency, statusCode, response.body());

        if (statusCode >= 200 && statusCode < 300) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).path("message");
                String content = message.path("content").asText();
                if (content != null && !content.trim().isEmpty()) {
                    return content;
                }
            }
            throw new IOException("Groq returned empty response: " + response.body());
        }

        throw new IOException("Groq API error (HTTP " + statusCode + "): " + response.body());
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
