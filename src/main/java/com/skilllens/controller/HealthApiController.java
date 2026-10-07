package com.skilllens.controller;

import com.skilllens.service.security.CredentialManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST API Controller for application health, telemetry, and runtime configuration status.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthApiController {

    private final CredentialManager credentialManager;

    public HealthApiController(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "UP");
        status.put("version", "1.0.0-Java25");
        status.put("runtime", "Java " + System.getProperty("java.version"));
        status.put("timestamp", Instant.now().toString());

        Map<String, Object> aiProviders = new LinkedHashMap<>();
        aiProviders.put("primary", Map.of(
                "provider", "Google Gemini",
                "defaultModel", "gemini-3.5-flash",
                "configured", credentialManager.hasApiKey(CredentialManager.KEY_GEMINI)
        ));
        aiProviders.put("fallback", Map.of(
                "provider", "Groq",
                "defaultModel", "openai/gpt-oss-120b",
                "configured", credentialManager.hasApiKey(CredentialManager.KEY_GROQ)
        ));
        status.put("ai", aiProviders);

        status.put("integrations", Map.of(
                "gitHubTokenConfigured", credentialManager.hasApiKey(CredentialManager.KEY_GITHUB),
                "youTubeKeyConfigured", credentialManager.hasApiKey(CredentialManager.KEY_YOUTUBE)
        ));

        return ResponseEntity.ok(status);
    }
}
