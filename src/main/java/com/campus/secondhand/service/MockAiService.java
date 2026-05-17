package com.campus.secondhand.service;

public class MockAiService implements AiService {

    @Override
    public String optimizeDescription(String rawDescription) {
        if (rawDescription == null || rawDescription.trim().isEmpty()) {
            return "请先填写商品描述，系统会在后续版本中生成优化建议。";
        }
        return "Mock 优化建议：将描述补充为更具体的成色、使用时长、配件和交易说明。";
    }

    @Override
    public String buildOperationsSummary() {
        return "Mock 运营摘要：当前为第一阶段骨架，后续将接入真实 AI 分析。";
    }
}
