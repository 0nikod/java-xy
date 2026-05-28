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
	public String optimizeDescriptionStreaming(String rawDescription, Consumer<String> onDelta) {
		try {
			return primary.optimizeDescriptionStreaming(rawDescription, onDelta);
		} catch (Exception ex) {
			return fallback.optimizeDescriptionStreaming(rawDescription, onDelta);
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
