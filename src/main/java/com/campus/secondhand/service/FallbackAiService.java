package com.campus.secondhand.service;

import java.util.function.Consumer;

/**
 * AI 调用兜底实现。
 * <p>
 * 优先尝试真实 AI 服务；当网络、鉴权或模型返回异常时，自动退回到本地 Mock 结果。
 */
public class FallbackAiService implements AiService {

	private final AiService primary;
	private final AiService fallback;

	public FallbackAiService(AiService primary, AiService fallback) {
		this.primary = primary;
		this.fallback = fallback;
	}

	@Override
	public String optimizeDescription(String rawDescription) {
		// 商品描述优化优先走真实模型，失败时切换到本地兜底结果，保证页面可用。
		try {
			return primary.optimizeDescription(rawDescription);
		} catch (Exception ignored) {
			return fallback.optimizeDescription(rawDescription);
		}
	}

	@Override
	public String suggestPrice(String title, String category, double originalPrice, int conditionLevel, String description) {
		try {
			return primary.suggestPrice(title, category, originalPrice, conditionLevel, description);
		} catch (Exception ignored) {
			return fallback.suggestPrice(title, category, originalPrice, conditionLevel, description);
		}
	}

	@Override
	public String buildPurchaseAdvice(String goodsContext) {
		try {
			return primary.buildPurchaseAdvice(goodsContext);
		} catch (Exception ignored) {
			return fallback.buildPurchaseAdvice(goodsContext);
		}
	}

	@Override
	public String assistSearch(String userInput) {
		try {
			return primary.assistSearch(userInput);
		} catch (Exception ignored) {
			return fallback.assistSearch(userInput);
		}
	}

	@Override
	public String buildOperationsSummary(String context) {
		// 运营摘要同样采用“先真实、后兜底”的策略，避免后台首页被 AI 故障阻塞。
		try {
			return primary.buildOperationsSummary(context);
		} catch (Exception ignored) {
			return fallback.buildOperationsSummary(context);
		}
	}

	@Override
	public String interpretStatistics(String statsContext) {
		try {
			return primary.interpretStatistics(statsContext);
		} catch (Exception ignored) {
			return fallback.interpretStatistics(statsContext);
		}
	}
	@Override
	public String optimizeDescriptionStreaming(String rawDescription, Consumer<String> onDelta) {
		try {
			return primary.optimizeDescriptionStreaming(rawDescription, onDelta);
		} catch (Exception ex) {
			return fallback.optimizeDescriptionStreaming(rawDescription, onDelta);
		}
	}

	@Override
	public String suggestPriceStreaming(String title, String category, double originalPrice, int conditionLevel,
			String description, Consumer<String> onDelta) {
		try {
			return primary.suggestPriceStreaming(title, category, originalPrice, conditionLevel, description, onDelta);
		} catch (Exception ex) {
			return fallback.suggestPriceStreaming(title, category, originalPrice, conditionLevel, description, onDelta);
		}
	}

	@Override
	public String buildPurchaseAdviceStreaming(String goodsContext, Consumer<String> onDelta) {
		try {
			return primary.buildPurchaseAdviceStreaming(goodsContext, onDelta);
		} catch (Exception ex) {
			return fallback.buildPurchaseAdviceStreaming(goodsContext, onDelta);
		}
	}

	@Override
	public String assistSearchStreaming(String userInput, Consumer<String> onDelta) {
		try {
			return primary.assistSearchStreaming(userInput, onDelta);
		} catch (Exception ex) {
			return fallback.assistSearchStreaming(userInput, onDelta);
		}
	}

	@Override
	public String buildOperationsSummaryStreaming(String context, Consumer<String> onDelta) {
		try {
			return primary.buildOperationsSummaryStreaming(context, onDelta);
		} catch (Exception ex) {
			return fallback.buildOperationsSummaryStreaming(context, onDelta);
		}
	}

	@Override
	public String interpretStatisticsStreaming(String statsContext, Consumer<String> onDelta) {
		try {
			return primary.interpretStatisticsStreaming(statsContext, onDelta);
		} catch (Exception ex) {
			return fallback.interpretStatisticsStreaming(statsContext, onDelta);
		}
	}

}
