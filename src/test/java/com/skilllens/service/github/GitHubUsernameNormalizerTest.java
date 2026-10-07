package com.skilllens.service.github;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GitHubUsernameNormalizerTest {

    @ParameterizedTest
    @DisplayName("Successfully normalizes valid usernames, URLs, and queries")
    @CsvSource({
            "johnsmith, johnsmith",
            "@johnsmith, johnsmith",
            "https://github.com/johnsmith, johnsmith",
            "https://github.com/johnsmith/, johnsmith",
            "https://github.com/johnsmith?tab=repositories, johnsmith",
            "github.com/johnsmith, johnsmith",
            "http://github.com/johnsmith, johnsmith",
            "https://www.github.com/johnsmith, johnsmith",
            "  johnsmith  , johnsmith",
            "john-smith-123, john-smith-123",
            "https://github.com/johnsmith#contributions, johnsmith"
    })
    void testValidNormalizations(String input, String expected) {
        assertEquals(expected, GitHubUsernameNormalizer.normalize(input));
        assertTrue(GitHubUsernameNormalizer.isValid(input));
    }

    @ParameterizedTest
    @DisplayName("Rejects invalid URLs, empty strings, and malformed usernames")
    @ValueSource(strings = {
            "",
            "   ",
            "https://gitlab.com/johnsmith",
            "https://linkedin.com/in/johnsmith",
            "-invalidstart",
            "invalid--doublehyphen",
            "user with spaces",
            "https://github.com/",
            "https://github.com"
    })
    void testInvalidInputs(String input) {
        assertThrows(IllegalArgumentException.class, () -> GitHubUsernameNormalizer.normalize(input));
        assertFalse(GitHubUsernameNormalizer.isValid(input));
    }

    @Test
    @DisplayName("Rejects null input")
    void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> GitHubUsernameNormalizer.normalize(null));
        assertFalse(GitHubUsernameNormalizer.isValid(null));
    }
}
