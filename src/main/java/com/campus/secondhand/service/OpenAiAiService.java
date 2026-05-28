package com.campus.secondhand.service;

import com.campus.secondhand.config.AppConfig;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import java.util.function.Consumer;

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
	public String optimizeDescriptionStreaming(String rawDescription, Consumer<String> onDelta) {
		return requestTextStreaming("你是校园二手平台商品发布助手。请在不虚构信息的前提下，优化输入描述，输出简洁自然的中文结果。",
				rawDescription == null ? "" : rawDescription, onDelta);
	}

	@Override
	public String buildPurchaseAdviceStreaming(String goodsContext, Consumer<String> onDelta) {
		return requestTextStreaming("你是校园二手平台购买顾问。请根据商品信息给出简短购买建议、风险点和验货提醒。",
				goodsContext == null ? "" : goodsContext, onDelta);
	}

	@Override
	public String assistSearchStreaming(String userInput, Consumer<String> onDelta) {
		return requestTextStreaming("你是校园二手平台搜索助手。请把用户自然语言需求改写为搜索关键词、推荐分类和排序建议。",
				userInput == null ? "" : userInput, onDelta);
	}

	@Override
	public String buildOperationsSummaryStreaming(String context, Consumer<String> onDelta) {
		return requestTextStreaming("你是校园二手平台运营助理。请根据输入内容输出 3 句以内的中文运营摘要，不要使用 Markdown。",
				context == null ? "" : context, onDelta);
	}

	@Override
	public String interpretStatisticsStreaming(String statsContext, Consumer<String> onDelta) {
		return requestTextStreaming("你是校园二手平台数据分析助手。请解读统计图表，指出运营现状、风险和改进建议，控制在 4 句以内。",
				statsContext == null ? "" : statsContext, onDelta);
	}

	private String requestTextStreaming(String instructions, String input, Consumer<String> onDelta) {
		StringBuilder builder = new StringBuilder();
		try {
			return requestTextResponsesStreaming(instructions, input, onDelta, builder);
		} catch (RuntimeException ex) {
			if (builder.length() > 0) {
				throw ex;
			}
			return requestTextChatStreaming(instructions, input, onDelta);
		}
	}

	private String requestTextResponsesStreaming(String instructions, String input, Consumer<String> onDelta,
			StringBuilder builder) {
		ResponseCreateParams params = ResponseCreateParams.builder().model(AppConfig.getOpenAiModel())
				.instructions(instructions).input(input).maxOutputTokens(1024L).build();
		try (StreamResponse<ResponseStreamEvent> stream = client.responses().createStreaming(params)) {
			stream.stream().forEach(event -> {
				if (event.isOutputTextDelta()) {
					emitDelta(builder, onDelta, event.asOutputTextDelta().delta());
				} else if (event.isError()) {
					throw new IllegalStateException("AI 流式响应错误：" + event.asError().message());
				} else if (event.isFailed()) {
					throw new IllegalStateException("AI 流式响应失败：" + event.asFailed().response().error()
							.map(error -> error.message()).orElse("未知错误"));
				} else if (event.isIncomplete()) {
					throw new IllegalStateException("AI 流式响应未完整结束");
				}
			});
		}
		return requireText(builder);
	}

	private String requestTextChatStreaming(String instructions, String input, Consumer<String> onDelta) {
		ChatCompletionCreateParams params = ChatCompletionCreateParams.builder().model(AppConfig.getOpenAiModel())
				.addSystemMessage(instructions).addUserMessage(input).maxTokens(1024L).build();
		StringBuilder builder = new StringBuilder();
		try (StreamResponse<ChatCompletionChunk> stream = client.chat().completions().createStreaming(params)) {
			stream.stream().forEach(chunk -> {
				for (ChatCompletionChunk.Choice choice : chunk.choices()) {
					choice.delta().content().ifPresent(delta -> emitDelta(builder, onDelta, delta));
				}
			});
		}
		return requireText(builder);
	}

	private void emitDelta(StringBuilder builder, Consumer<String> onDelta, String delta) {
		if (delta != null && !delta.isEmpty()) {
			builder.append(delta);
			if (onDelta != null) {
				onDelta.accept(delta);
			}
		}
	}

	private String requireText(StringBuilder builder) {
		if (builder.length() == 0) {
			throw new IllegalStateException("AI 未返回文本内容");
		}
		return builder.toString().trim();
	}
}
