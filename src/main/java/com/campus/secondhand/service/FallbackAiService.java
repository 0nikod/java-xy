package com.campus.secondhand.service;

public class FallbackAiService implements AiService {

    private final AiService primary;
    private final AiService fallback;

    public FallbackAiService(AiService primary, AiService fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public String optimizeDescription(String rawDescription) {
        try {
            return primary.optimizeDescription(rawDescription);
        } catch (Exception ignored) {
            return fallback.optimizeDescription(rawDescription);
        }
    }

    @Override
    public String buildOperationsSummary(String context) {
        try {
            return primary.buildOperationsSummary(context);
        } catch (Exception ignored) {
            return fallback.buildOperationsSummary(context);
        }
    }
}
