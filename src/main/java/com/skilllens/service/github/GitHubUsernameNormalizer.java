package com.skilllens.service.github;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes user input into a canonical GitHub username.
 * Accepts:
 * - johnsmith
 * - @johnsmith
 * - https://github.com/johnsmith
 * - https://github.com/johnsmith/
 * - https://github.com/johnsmith?tab=repositories
 * - github.com/johnsmith
 *
 * Rejects:
 * - empty/null strings
 * - non-GitHub URLs (e.g. linkedin.com/in/john)
 * - invalid GitHub username characters
 */
public final class GitHubUsernameNormalizer {

    private static final Pattern VALID_USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$");

    private GitHubUsernameNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null) {
            throw new IllegalArgumentException("GitHub username or URL cannot be null");
        }

        String cleaned = input.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("GitHub username or URL cannot be empty");
        }

        // Strip leading @
        if (cleaned.startsWith("@")) {
            cleaned = cleaned.substring(1).trim();
        }

        // If it starts with http://, https://, or github.com/
        if (cleaned.contains("github.com")) {
            if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
                cleaned = "https://" + cleaned;
            }
            try {
                URI uri = URI.create(cleaned);
                String host = uri.getHost();
                if (host == null || (!host.equalsIgnoreCase("github.com") && !host.equalsIgnoreCase("www.github.com"))) {
                    throw new IllegalArgumentException("Invalid domain. Only github.com URLs are accepted: " + input);
                }

                String path = uri.getPath();
                if (path == null || path.isEmpty() || path.equals("/")) {
                    throw new IllegalArgumentException("No GitHub username found in URL: " + input);
                }

                // Path like /username or /username/ or /username/repos
                String[] segments = path.split("/");
                for (String seg : segments) {
                    String trimmed = seg.trim();
                    if (!trimmed.isEmpty()) {
                        cleaned = trimmed;
                        break;
                    }
                }
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }
                throw new IllegalArgumentException("Malformed GitHub URL: " + input, e);
            }
        } else if (cleaned.contains("/")) {
            // e.g. "username/"
            String[] segments = cleaned.split("/");
            if (segments.length > 0 && !segments[0].trim().isEmpty()) {
                cleaned = segments[0].trim();
            }
        }

        // Strip any remaining query parameters or fragments if pasted without protocol
        if (cleaned.contains("?")) {
            cleaned = cleaned.substring(0, cleaned.indexOf('?')).trim();
        }
        if (cleaned.contains("#")) {
            cleaned = cleaned.substring(0, cleaned.indexOf('#')).trim();
        }

        Matcher matcher = VALID_USERNAME_PATTERN.matcher(cleaned);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid GitHub username '" + cleaned + "'. Must be 1-39 alphanumeric characters and single hyphens.");
        }

        return cleaned;
    }

    public static boolean isValid(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            normalize(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidUsername(String input) {
        return isValid(input);
    }
}
