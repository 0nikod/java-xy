package com.campus.secondhand.service;

import com.campus.secondhand.config.AppConfig;

public final class AiServiceFactory {

    private AiServiceFactory() {
    }

    public static AiService create() {
        if (!AppConfig.isOpenAiConfigured()) {
            return new MockAiService();
        }
        return new FallbackAiService(new OpenAiAiService(), new MockAiService());
    }
}
