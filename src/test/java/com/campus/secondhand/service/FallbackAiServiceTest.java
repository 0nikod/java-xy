package com.campus.secondhand.service;

import org.junit.Assert;
import org.junit.Test;

/**
 * 验证 AI 服务失败时可以正确切换到兜底实现。
 */
public class FallbackAiServiceTest {

	@Test
	public void optimizeDescriptionShouldFallbackWhenPrimaryFails() {
		AiService fallback = new MockAiService();

		String result = new FallbackAiService(failingAiService(), fallback).optimizeDescriptionStreaming("旧描述", null);
		Assert.assertTrue(result.startsWith("Mock 优化建议"));
	}

	@Test
	public void buildOperationsSummaryShouldFallbackWhenPrimaryFails() {
		AiService fallback = new MockAiService();

		String result = new FallbackAiService(failingAiService(), fallback).buildOperationsSummaryStreaming("统计上下文", null);
		Assert.assertEquals("Mock 运营摘要：统计上下文", result);
	}

	@Test
	public void newAiFeaturesShouldFallbackWhenPrimaryFails() {
		AiService service = new FallbackAiService(failingAiService(), new MockAiService());

		Assert.assertTrue(service.buildPurchaseAdviceStreaming("商品上下文", null).startsWith("Mock 购买建议"));
		Assert.assertTrue(service.assistSearchStreaming("便宜教材", null).startsWith("Mock 搜索辅助"));
		Assert.assertTrue(service.interpretStatisticsStreaming("统计上下文", null).startsWith("Mock 图表解读"));
	}

	private AiService failingAiService() {
		return new AiService() {
			@Override
			public String optimizeDescriptionStreaming(String rawDescription, java.util.function.Consumer<String> onDelta) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String buildPurchaseAdviceStreaming(String goodsContext, java.util.function.Consumer<String> onDelta) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String assistSearchStreaming(String userInput, java.util.function.Consumer<String> onDelta) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String buildOperationsSummaryStreaming(String context, java.util.function.Consumer<String> onDelta) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String interpretStatisticsStreaming(String statsContext, java.util.function.Consumer<String> onDelta) {
				throw new IllegalStateException("boom");
			}
		};
	}
}
