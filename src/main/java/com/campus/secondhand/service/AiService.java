package com.campus.secondhand.service;

import java.util.function.Consumer;

public interface AiService {

	String optimizeDescriptionStreaming(String rawDescription, Consumer<String> onDelta);

	String buildPurchaseAdviceStreaming(String goodsContext, Consumer<String> onDelta);

	String assistSearchStreaming(String userInput, Consumer<String> onDelta);

	String buildOperationsSummaryStreaming(String context, Consumer<String> onDelta);

	String interpretStatisticsStreaming(String statsContext, Consumer<String> onDelta);
}
