package com.campus.secondhand.service;

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
	public String buildOperationsSummary(String context) {
		// 运营摘要同样采用“先真实、后兜底”的策略，避免后台首页被 AI 故障阻塞。
		try {
			return primary.buildOperationsSummary(context);
		} catch (Exception ignored) {
			return fallback.buildOperationsSummary(context);
		}
	}
}
