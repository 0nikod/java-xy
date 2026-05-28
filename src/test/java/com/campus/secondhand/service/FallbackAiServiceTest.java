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

		String result = new FallbackAiService(failingAiService(), fallback).optimizeDescription("旧描述");
		Assert.assertTrue(result.startsWith("Mock 优化建议"));
	}

	@Test
	public void buildOperationsSummaryShouldFallbackWhenPrimaryFails() {
		AiService fallback = new MockAiService();

		String result = new FallbackAiService(failingAiService(), fallback).buildOperationsSummary("统计上下文");
		Assert.assertEquals("Mock 运营摘要：统计上下文", result);
	}

	@Test
	public void newAiFeaturesShouldFallbackWhenPrimaryFails() {
		AiService service = new FallbackAiService(failingAiService(), new MockAiService());

		Assert.assertTrue(service.suggestPrice("教材", "教材", 100.0, 8, "很新").startsWith("Mock 定价建议"));
		Assert.assertTrue(service.buildPurchaseAdvice("商品上下文").startsWith("Mock 购买建议"));
		Assert.assertTrue(service.assistSearch("便宜教材").startsWith("Mock 搜索辅助"));
		Assert.assertTrue(service.analyzeViolationRisk("商品上下文").startsWith("Mock 违规风险分析"));
		Assert.assertTrue(service.interpretStatistics("统计上下文").startsWith("Mock 图表解读"));
	}

	private AiService failingAiService() {
		return new AiService() {
			@Override
			public String optimizeDescription(String rawDescription) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String suggestPrice(String title, String category, double originalPrice, int conditionLevel,
					String description) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String buildPurchaseAdvice(String goodsContext) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String assistSearch(String userInput) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String buildOperationsSummary(String context) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String analyzeViolationRisk(String goodsContext) {
				throw new IllegalStateException("boom");
			}

			@Override
			public String interpretStatistics(String statsContext) {
				throw new IllegalStateException("boom");
			}
		};
	}
}
