package com.campus.secondhand.service;

import org.junit.Assert;
import org.junit.Test;

/**
 * 验证 AI 服务失败时可以正确切换到兜底实现。
 */
public class FallbackAiServiceTest {

	@Test
	public void optimizeDescriptionShouldFallbackWhenPrimaryFails() {
		// 主服务抛异常时，应回退到本地 Mock 结果，保证优化功能可用。
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
		// 运营摘要同样需要验证兜底路径，避免后台统计页面受 AI 故障影响。
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
