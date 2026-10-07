package com.skilllens.domain.model;

/**
 * Encapsulates the result and execution telemetry of an AI invocation.
 */
public record AiExecutionResult(
        String content,
        String provider,
        String model,
        long latencyMs,
        boolean fallbackTriggered,
        String fallbackReason
) {
    public static AiExecutionResult primary(String content, String model, long latencyMs) {
        return new AiExecutionResult(content, "GEMINI", model, latencyMs, false, null);
    }

    public static AiExecutionResult fallback(String content, String model, long latencyMs, String reason) {
        return new AiExecutionResult(content, "GROQ", model, latencyMs, true, reason);
    }
}
