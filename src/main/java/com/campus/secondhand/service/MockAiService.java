package com.campus.secondhand.service;

/**
 * 本地 Mock AI 实现。
 * <p>
 * 用于离线演示或真实模型不可用时返回稳定的可读文本。
 */
public class MockAiService implements AiService {

	@Override
	public String optimizeDescription(String rawDescription) {
		// 没有输入时给出提示；有输入时返回一段固定的优化建议，保证页面有结果可展示。
		if (rawDescription == null || rawDescription.trim().isEmpty()) {
			return "请先填写商品描述，系统会在后续版本中生成优化建议。";
		}
		return "Mock 优化建议：将描述补充为更具体的成色、使用时长、配件和交易说明。";
	}

	@Override
	public String suggestPrice(String title, String category, double originalPrice, int conditionLevel, String description) {
		double base = originalPrice <= 0 ? 50.0 : originalPrice;
		double ratio = Math.max(0.2, Math.min(0.9, conditionLevel / 10.0));
		double low = base * ratio * 0.75;
		double high = base * ratio * 0.9;
		return String.format("Mock 定价建议：%s/%s 可考虑 %.2f - %.2f 元，理由是成色约 %d 成且需留出校园二手议价空间。",
				title == null ? "该商品" : title, category == null ? "未分类" : category, low, high, conditionLevel);
	}

	@Override
	public String buildPurchaseAdvice(String goodsContext) {
		return "Mock 购买建议：建议重点确认商品成色、配件完整度、图片是否与描述一致，并优先选择线下当面验货交易。" + normalizeContext(goodsContext);
	}

	@Override
	public String assistSearch(String userInput) {
		if (userInput == null || userInput.trim().isEmpty()) {
			return "Mock 搜索辅助：请输入想买的商品、预算或用途，我会建议关键词和分类。";
		}
		return "Mock 搜索辅助：可尝试提取关键词“" + userInput.trim() + "”，并结合分类、价格升序或成色优先筛选。";
	}

	@Override
	public String buildOperationsSummary(String context) {
		// 运营摘要在离线模式下直接回显上下文，方便演示流程不中断。
		if (context == null || context.trim().isEmpty()) {
			return "Mock 运营摘要：当前为离线演示模式，系统会展示模拟分析结果。";
		}
		return "Mock 运营摘要：" + context;
	}

	@Override
	public String interpretStatistics(String statsContext) {
		return "Mock 图表解读：当前统计图可用于观察分类供给、商品状态和近期成交变化；建议优先处理待审核商品并关注成交低迷分类。" + normalizeContext(statsContext);
	}

	private String normalizeContext(String context) {
		return context == null || context.trim().isEmpty() ? "" : " 输入摘要：" + context.trim();
	}
}
