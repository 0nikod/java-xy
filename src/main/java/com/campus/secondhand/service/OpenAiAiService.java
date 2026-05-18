package com.campus.secondhand.service;

import com.campus.secondhand.config.AppConfig;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

public class OpenAiAiService implements AiService {

    private final OpenAIClient client;

    public OpenAiAiService() {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(AppConfig.getOpenAiApiKey())
                .baseUrl(AppConfig.getOpenAiBaseUrl())
                .build();
    }

    @Override
    public String optimizeDescription(String rawDescription) {
        return requestText(
                "你是校园二手平台商品发布助手。请在不虚构信息的前提下，优化输入描述，输出简洁自然的中文结果。",
                rawDescription == null ? "" : rawDescription);
    }

    @Override
    public String buildOperationsSummary(String context) {
        return requestText(
                "你是校园二手平台运营助理。请根据输入内容输出 3 句以内的中文运营摘要，不要使用 Markdown。",
                context == null ? "" : context);
    }

    private String requestText(String instructions, String input) {
        Response response = client.responses().create(ResponseCreateParams.builder()
                .model(AppConfig.getOpenAiModel())
                .instructions(instructions)
                .input(input)
                .maxOutputTokens(240L)
                .build());

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
