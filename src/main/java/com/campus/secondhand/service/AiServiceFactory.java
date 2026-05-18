package com.campus.secondhand.service;

import com.campus.secondhand.config.AppConfig;

/**
 * AI 服务工厂。
 * <p>
 * 根据当前配置决定使用真实模型、Mock，还是带兜底的混合实现。
 */
public final class AiServiceFactory {

	private AiServiceFactory() {
	}

	public static AiService create() {
		// 未配置 OpenAI 时直接使用 Mock，确保离线环境也能演示。
		if (!AppConfig.isOpenAiConfigured()) {
			return new MockAiService();
		}
		// 已配置时使用真实服务 + Mock 兜底，兼顾效果与稳定性。
		return new FallbackAiService(new OpenAiAiService(), new MockAiService());
	}
}
