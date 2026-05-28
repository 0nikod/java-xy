package com.campus.secondhand.service;

/**
 * 本地 Mock AI 实现。
 * <p>
 * 用于离线演示或真实模型不可用时返回稳定的可读文本。
 */
public class MockAiService implements AiService {

	@Override
	public String optimizeDescriptionStreaming(String rawDescription, java.util.function.Consumer<String> onDelta) {
		String result;
		if (rawDescription == null || rawDescription.trim().isEmpty()) {
			result = "请先填写商品描述，系统会在后续版本中生成优化建议。";
		} else {
			result = "Mock 优化建议：将描述补充为更具体的成色、使用时长、配件和交易说明。";
		}
		return emitResult(result, onDelta);
	}

	@Override
	public String buildPurchaseAdviceStreaming(String goodsContext, java.util.function.Consumer<String> onDelta) {
		return emitResult("Mock 购买建议：建议重点确认商品成色、配件完整度、图片是否与描述一致，并优先选择线下当面验货交易。" + normalizeContext(goodsContext), onDelta);
	}

	@Override
	public String assistSearchStreaming(String userInput, java.util.function.Consumer<String> onDelta) {
		String result;
		if (userInput == null || userInput.trim().isEmpty()) {
			result = "Mock 搜索辅助：请输入想买的商品、预算或用途，我会建议关键词和分类。";
		} else {
			result = "Mock 搜索辅助：可尝试提取关键词“" + userInput.trim() + "”，并结合分类、价格升序或成色优先筛选。";
		}
		return emitResult(result, onDelta);
	}

	@Override
	public String buildOperationsSummaryStreaming(String context, java.util.function.Consumer<String> onDelta) {
		String result;
		if (context == null || context.trim().isEmpty()) {
			result = "Mock 运营摘要：当前为离线演示模式，系统会展示模拟分析结果。";
		} else {
			result = "Mock 运营摘要：" + context;
		}
		return emitResult(result, onDelta);
	}

	@Override
	public String interpretStatisticsStreaming(String statsContext, java.util.function.Consumer<String> onDelta) {
		return emitResult("Mock 图表解读：当前统计图可用于观察分类供给、商品状态和近期成交变化；建议优先处理待审核商品并关注成交低迷分类。" + normalizeContext(statsContext), onDelta);
	}

	private String normalizeContext(String context) {
		return context == null || context.trim().isEmpty() ? "" : " 输入摘要：" + context.trim();
	}

	private String emitResult(String result, java.util.function.Consumer<String> onDelta) {
		if (onDelta != null && result != null && !result.isEmpty()) {
			onDelta.accept(result);
		}
		return result;
	}
}
