package com.skilllens.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized credential manager.
 * Resolves credentials with strict priority:
 * 1. OS-backed secure storage (Windows DPAPI vault) - primary for user-entered keys in packaged app
 * 2. System Environment Variables (e.g. GEMINI_API_KEY, GROQ_API_KEY)
 * 3. Local development .env file (development mode convenience only)
 *
 * Never hardcodes credentials or prints unredacted keys.
 */
@Service
public class CredentialManager {

    private static final Logger log = LoggerFactory.getLogger(CredentialManager.class);

    public static final String KEY_GEMINI = "GEMINI_API_KEY";
    public static final String KEY_GROQ = "GROQ_API_KEY";
    public static final String KEY_GITHUB = "GITHUB_TOKEN";
    public static final String KEY_YOUTUBE = "YOUTUBE_API_KEY";

    private final SecureCredentialStorageService secureVault;
    private final Map<String, String> dotEnvDevCache = new HashMap<>();

    public CredentialManager(SecureCredentialStorageService secureVault) {
        this.secureVault = secureVault;
        loadDevelopmentDotEnv();
    }

    private void loadDevelopmentDotEnv() {
        Path dotEnvPath = Paths.get(".env");
        if (Files.isRegularFile(dotEnvPath)) {
            try {
                for (String line : Files.readAllLines(dotEnvPath)) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        int eqIdx = trimmed.indexOf('=');
                        String k = trimmed.substring(0, eqIdx).trim();
                        String v = trimmed.substring(eqIdx + 1).trim();
                        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                            v = v.substring(1, v.length() - 1);
                        } else if (v.startsWith("'") && v.endsWith("'") && v.length() >= 2) {
                            v = v.substring(1, v.length() - 1);
                        }
                        if (!v.isEmpty()) {
                            dotEnvDevCache.put(k, v);
                        }
                    }
                }
            } catch (IOException e) {
                log.debug("No development .env file loaded: {}", e.getMessage());
            }
        }
    }

    public Optional<String> getApiKey(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) {
            return Optional.empty();
        }

        // 1. Check OS-backed secure vault
        Optional<String> vaultVal = secureVault.getCredential(keyName);
        if (vaultVal.isPresent() && !vaultVal.get().trim().isEmpty()) {
            return vaultVal;
        }

        // 2. Check System Environment variables
        String envVal = System.getenv(keyName);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return Optional.of(envVal.trim());
        }

        // 3. Check System properties
        String propVal = System.getProperty(keyName);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return Optional.of(propVal.trim());
        }

        // 4. Check local development .env cache
        String dotEnvVal = dotEnvDevCache.get(keyName);
        if (dotEnvVal != null && !dotEnvVal.trim().isEmpty()) {
            return Optional.of(dotEnvVal.trim());
        }

        return Optional.empty();
    }

    public void setApiKey(String keyName, String value) {
        if (value == null || value.trim().isEmpty()) {
            secureVault.deleteCredential(keyName);
        } else {
            secureVault.saveCredential(keyName, value.trim());
        }
    }

    public boolean hasApiKey(String keyName) {
        return getApiKey(keyName).isPresent();
    }

    public static String mask(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            return "NOT CONFIGURED";
        }
        int len = secret.length();
        if (len <= 8) {
            return "********";
        }
        return secret.substring(0, 4) + "..." + secret.substring(len - 4);
    }
}
