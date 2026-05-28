package com.campus.secondhand.service;

import java.util.function.Consumer;

public interface AiService {

	String optimizeDescription(String rawDescription);

	default String optimizeDescriptionStreaming(String rawDescription, Consumer<String> onDelta) {
		return emit(optimizeDescription(rawDescription), onDelta);
	}

	String suggestPrice(String title, String category, double originalPrice, int conditionLevel, String description);

	default String suggestPriceStreaming(String title, String category, double originalPrice, int conditionLevel,
			String description, Consumer<String> onDelta) {
		return emit(suggestPrice(title, category, originalPrice, conditionLevel, description), onDelta);
	}

	String buildPurchaseAdvice(String goodsContext);

	default String buildPurchaseAdviceStreaming(String goodsContext, Consumer<String> onDelta) {
		return emit(buildPurchaseAdvice(goodsContext), onDelta);
	}

	String assistSearch(String userInput);

	default String assistSearchStreaming(String userInput, Consumer<String> onDelta) {
		return emit(assistSearch(userInput), onDelta);
	}

	String buildOperationsSummary(String context);

	default String buildOperationsSummaryStreaming(String context, Consumer<String> onDelta) {
		return emit(buildOperationsSummary(context), onDelta);
	}

	String interpretStatistics(String statsContext);

	default String interpretStatisticsStreaming(String statsContext, Consumer<String> onDelta) {
		return emit(interpretStatistics(statsContext), onDelta);
	}

	default String emit(String result, Consumer<String> onDelta) {
		if (onDelta != null && result != null && !result.isEmpty()) {
			onDelta.accept(result);
		}
		return result;
	}
}
