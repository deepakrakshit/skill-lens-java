---
name: skill-lens-data-privacy
description: >-
  Enforces zero-hardcoding credential invariants, OS-backed secure credential storage,
  student PII protection, GitHub privacy boundaries, and prompt-injection defenses.
  Use when handling API keys, storing credentials, managing sensitive student data, or enforcing privacy boundaries.
---

# SkillLens Data Privacy & Credential Security

This skill defines the technical standards for protecting user credentials, safeguarding student PII, and maintaining ethical boundaries during GitHub and resume analysis.

---

## 1. Top-Level Credential Invariants

- **NEVER Hardcode Credentials:** No API keys, personal access tokens, or secrets may be compiled into the Java binary or committed to version control.
- **`.env` Is NOT for End-Users:**
  - Environment variables and `.env` files are strictly for local developer/demo usage.
  - The packaged desktop application provides an interactive **Settings UI** where the student enters their own Gemini, Groq, GitHub, and optional YouTube credentials.
- **FORBIDDEN Credential Storage Locations:**
  - NEVER store credentials in SQLite / H2 database tables.
  - NEVER store credentials in plaintext JSON config files or exports.
  - NEVER print credentials in log files (`*.log`), error traces, or console output.
  - Provide `.env.example` solely as a development convenience.

---

## 2. OS-Backed Secure Credential Storage

The desktop application stores user-configured credentials using platform-native operating system credential managers:

### Architecture:
- **Windows:** Windows Credential Manager via Windows Data Protection API (DPAPI / `CryptProtectData`).
- **macOS:** Apple Keychain Services (`Security.framework`).
- **Linux:** Secret Service API / `libsecret` via Freedesktop D-Bus.
- **Java Abstraction:** Centralized `SecureCredentialStorageService` interface providing:
  ```java
  public interface SecureCredentialStorageService {
      void saveCredential(String keyName, String secretValue);
      Optional<String> getCredential(String keyName);
      void deleteCredential(String keyName);
      boolean hasCredential(String keyName);
  }
  ```

---

## 3. Student PII Protection & Local Isolation

Student resumes contain sensitive Personally Identifiable Information (PII):
- **Local Persistence Only:** All candidate profile data, resume texts, and evaluation reports are stored strictly in the local embedded database on the user's workstation.
- **No Remote Telemetry:** The application does not transmit student personal profiles to external telemetry or tracking servers.
- **Prompt Sanitization:** When invoking LLM providers (Gemini / Groq), transmit only the technical and experiential text required for analysis. Strip phone numbers, physical addresses, and government identification numbers prior to network transmission.

---

## 4. GitHub Privacy & Consent Boundaries

- **Public Data Only:** By default, SkillLens accesses public repositories, public commits, and public profile data authorized by the student's entered username.
- **No Inference of Private Activity:** The system never guesses, infers, or claims knowledge of private repository contributions unless the user explicitly supplies an authorized token with private repository scope.
- **Zero Privacy Bypassing:** The application strictly respects GitHub API privacy boundaries and terms of service.

---

## 5. Untrusted Data & Injection Defense

All third-party external content (resumes, GitHub READMEs, commit messages) is categorized as **untrusted data**:
- Enforce strict structural delimiter separation in all model prompts.
- Sanitize HTML, script tags, and markdown escape sequences before rendering in JavaFX UI views to prevent UI injection attacks.
