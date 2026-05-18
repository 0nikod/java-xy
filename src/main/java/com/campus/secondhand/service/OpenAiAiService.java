package com.campus.secondhand.service;

import com.campus.secondhand.config.AppConfig;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

/**
 * 基于 OpenAI Java SDK 的真实 AI 实现。
 * <p>
 * 仅负责发起请求与提取文本，不承担兜底逻辑。
 */
public class OpenAiAiService implements AiService {

	private final OpenAIClient client;

	public OpenAiAiService() {
		// 客户端配置由 AppConfig 统一提供，避免把密钥和地址写死在服务实现里。
		this.client = OpenAIOkHttpClient.builder().apiKey(AppConfig.getOpenAiApiKey())
				.baseUrl(AppConfig.getOpenAiBaseUrl()).build();
	}

	@Override
	public String optimizeDescription(String rawDescription) {
		// 商品描述优化提示词尽量克制，避免模型虚构不存在的商品信息。
		return requestText("你是校园二手平台商品发布助手。请在不虚构信息的前提下，优化输入描述，输出简洁自然的中文结果。",
				rawDescription == null ? "" : rawDescription);
	}

	@Override
	public String buildOperationsSummary(String context) {
		// 运营摘要限制输出长度，保证后台首页展示简洁。
		return requestText("你是校园二手平台运营助理。请根据输入内容输出 3 句以内的中文运营摘要，不要使用 Markdown。", context == null ? "" : context);
	}

	private String requestText(String instructions, String input) {
		// 统一封装请求与响应解析，避免不同 AI 功能重复写提取逻辑。
		Response response = client.responses().create(ResponseCreateParams.builder().model(AppConfig.getOpenAiModel())
				.instructions(instructions).input(input).maxOutputTokens(240L).build());

		StringBuilder builder = new StringBuilder();
		for (ResponseOutputItem item : response.output()) {
			if (!item.isMessage()) {
				continue;
			}
			ResponseOutputMessage message = item.asMessage();
			for (ResponseOutputMessage.Content content : message.content()) {
				if (content.isOutputText()) {
					if (builder.length() > 0) {
						builder.append('\n');
					}
					builder.append(content.asOutputText().text());
				}
			}
		}
		if (builder.length() == 0) {
			throw new IllegalStateException("AI 未返回文本内容");
		}
		return builder.toString().trim();
	}
}
