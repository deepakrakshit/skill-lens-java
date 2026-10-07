package com.skilllens.service.security;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Crypt32;
import com.sun.jna.platform.win32.WinCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Windows DPAPI-backed secure credential storage.
 * Uses native Windows Data Protection API (CryptProtectData / CryptUnprotectData)
 * via JNA platform libraries.
 *
 * Encrypted blobs are persisted to ~/.skilllens/vault/.
 * On non-Windows platforms or test fallbacks, an isolated fallback vault is provided.
 */
@Service
public class WindowsDpapiVault implements SecureCredentialStorageService {

    private static final Logger log = LoggerFactory.getLogger(WindowsDpapiVault.class);

    private final Path vaultDirectory;
    private final Map<String, String> inMemoryFallbackCache = new ConcurrentHashMap<>();
    private final boolean isWindows;

    public WindowsDpapiVault() {
        this(Paths.get(System.getProperty("user.home"), ".skilllens", "vault"));
    }

    public WindowsDpapiVault(Path vaultDirectory) {
        this.vaultDirectory = vaultDirectory;
        this.isWindows = Platform.isWindows();
        try {
            Files.createDirectories(vaultDirectory);
        } catch (IOException e) {
            log.warn("Could not create secure vault directory: {}", vaultDirectory, e);
        }
    }

    @Override
    public synchronized void saveCredential(String keyName, String secretValue) {
        if (keyName == null || keyName.trim().isEmpty() || secretValue == null) {
            throw new IllegalArgumentException("Credential key name and secret value cannot be null or empty");
        }

        if (isWindows) {
            try {
                byte[] plaintextBytes = secretValue.getBytes(StandardCharsets.UTF_8);
                Memory memory = new Memory(plaintextBytes.length);
                memory.write(0, plaintextBytes, 0, plaintextBytes.length);

                WinCrypt.DATA_BLOB inBlob = new WinCrypt.DATA_BLOB();
                inBlob.cbData = plaintextBytes.length;
                inBlob.pbData = memory;

                WinCrypt.DATA_BLOB outBlob = new WinCrypt.DATA_BLOB();

                boolean success = Crypt32.INSTANCE.CryptProtectData(
                        inBlob,
                        keyName,
                        null,
                        null,
                        null,
                        0,
                        outBlob
                );

                if (success) {
                    byte[] encryptedBytes = outBlob.getData();
                    String base64Encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
                    Path keyFile = vaultDirectory.resolve(sanitizeKeyName(keyName) + ".enc");
                    Files.writeString(keyFile, base64Encrypted, StandardCharsets.UTF_8);
                    log.info("Securely saved credential '{}' using Windows DPAPI", keyName);
                    return;
                } else {
                    log.warn("CryptProtectData failed for '{}'. Using memory cache fallback.", keyName);
                }
            } catch (Throwable t) {
                log.warn("DPAPI encryption error for key '{}': {}", keyName, t.getMessage());
            }
        }

        // Cross-platform or non-Windows fallback
        inMemoryFallbackCache.put(keyName, secretValue);
    }

    @Override
    public synchronized Optional<String> getCredential(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) {
            return Optional.empty();
        }

        if (isWindows) {
            Path keyFile = vaultDirectory.resolve(sanitizeKeyName(keyName) + ".enc");
            if (Files.isRegularFile(keyFile)) {
                try {
                    String base64Encrypted = Files.readString(keyFile, StandardCharsets.UTF_8).trim();
                    byte[] encryptedBytes = Base64.getDecoder().decode(base64Encrypted);

                    Memory memory = new Memory(encryptedBytes.length);
                    memory.write(0, encryptedBytes, 0, encryptedBytes.length);

                    WinCrypt.DATA_BLOB inBlob = new WinCrypt.DATA_BLOB();
                    inBlob.cbData = encryptedBytes.length;
                    inBlob.pbData = memory;

                    WinCrypt.DATA_BLOB outBlob = new WinCrypt.DATA_BLOB();

                    boolean success = Crypt32.INSTANCE.CryptUnprotectData(
                            inBlob,
                            null,
                            null,
                            null,
                            null,
                            0,
                            outBlob
                    );

                    if (success) {
                        byte[] decryptedBytes = outBlob.getData();
                        return Optional.of(new String(decryptedBytes, StandardCharsets.UTF_8));
                    } else {
                        log.warn("CryptUnprotectData failed for key '{}'", keyName);
                    }
                } catch (Throwable t) {
                    log.warn("DPAPI decryption error for key '{}': {}", keyName, t.getMessage());
                }
            }
        }

        return Optional.ofNullable(inMemoryFallbackCache.get(keyName));
    }

    @Override
    public synchronized void deleteCredential(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) {
            return;
        }

        inMemoryFallbackCache.remove(keyName);
        Path keyFile = vaultDirectory.resolve(sanitizeKeyName(keyName) + ".enc");
        try {
            Files.deleteIfExists(keyFile);
            log.info("Deleted encrypted credential for '{}'", keyName);
        } catch (IOException e) {
            log.warn("Failed to delete encrypted file for '{}'", keyName, e);
        }
    }

    @Override
    public synchronized boolean hasCredential(String keyName) {
        return getCredential(keyName).isPresent();
    }

    private String sanitizeKeyName(String key) {
        return key.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
