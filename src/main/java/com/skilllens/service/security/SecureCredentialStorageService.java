package com.skilllens.service.security;

import java.util.Optional;

/**
 * OS-backed secure credential storage service interface.
 * Implemented using Windows DPAPI (CryptProtectData) on Windows,
 * with platform abstractions for macOS Keychain and Linux SecretService.
 */
public interface SecureCredentialStorageService {

    void saveCredential(String keyName, String secretValue);

    Optional<String> getCredential(String keyName);

    void deleteCredential(String keyName);

    boolean hasCredential(String keyName);
}
