package com.campus.secondhand.service;

import org.junit.Assert;
import org.junit.Test;

public class FallbackAiServiceTest {

    @Test
    public void optimizeDescriptionShouldFallbackWhenPrimaryFails() {
        AiService primary = new AiService() {
            @Override
            public String optimizeDescription(String rawDescription) {
                throw new IllegalStateException("boom");
            }

            @Override
            public String buildOperationsSummary(String context) {
                return "unused";
            }
        };
        AiService fallback = new MockAiService();

        String result = new FallbackAiService(primary, fallback).optimizeDescription("旧描述");
        Assert.assertTrue(result.startsWith("Mock 优化建议"));
    }

    @Test
    public void buildOperationsSummaryShouldFallbackWhenPrimaryFails() {
        AiService primary = new AiService() {
            @Override
            public String optimizeDescription(String rawDescription) {
                return "unused";
            }

            @Override
            public String buildOperationsSummary(String context) {
                throw new IllegalStateException("boom");
            }
        };
        AiService fallback = new MockAiService();

        String result = new FallbackAiService(primary, fallback).buildOperationsSummary("统计上下文");
        Assert.assertEquals("Mock 运营摘要：统计上下文", result);
    }
}
