package com.skilllens.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CredentialManagerTest {

    private CredentialManager credentialManager;
    private FakeSecureVault fakeVault;

    static class FakeSecureVault implements SecureCredentialStorageService {
        private final Map<String, String> store = new HashMap<>();

        @Override
        public void saveCredential(String keyName, String secretValue) {
            store.put(keyName, secretValue);
        }

        @Override
        public Optional<String> getCredential(String keyName) {
            return Optional.ofNullable(store.get(keyName));
        }

        @Override
        public void deleteCredential(String keyName) {
            store.remove(keyName);
        }

        @Override
        public boolean hasCredential(String keyName) {
            return store.containsKey(keyName);
        }
    }

    @BeforeEach
    void setUp() {
        fakeVault = new FakeSecureVault();
        credentialManager = new CredentialManager(fakeVault);
    }

    @Test
    @DisplayName("Prioritizes secure vault credentials over fallback stores")
    void testVaultPriority() {
        fakeVault.saveCredential(CredentialManager.KEY_GEMINI, "vault-gemini-secret-12345");

        Optional<String> keyOpt = credentialManager.getApiKey(CredentialManager.KEY_GEMINI);
        assertTrue(keyOpt.isPresent());
        assertEquals("vault-gemini-secret-12345", keyOpt.get());
    }

    @Test
    @DisplayName("Properly masks credentials without revealing full plaintext")
    void testMasking() {
        assertEquals("NOT CONFIGURED", CredentialManager.mask(null));
        assertEquals("NOT CONFIGURED", CredentialManager.mask(""));
        assertEquals("********", CredentialManager.mask("short"));
        assertEquals("AIza...7890", CredentialManager.mask("AIzaSyB1234567890"));
    }
}
