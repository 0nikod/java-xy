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
    public String buildOperationsSummary(String context) {
        // 运营摘要在离线模式下直接回显上下文，方便演示流程不中断。
        if (context == null || context.trim().isEmpty()) {
            return "Mock 运营摘要：当前为离线演示模式，系统会展示模拟分析结果。";
        }
        return "Mock 运营摘要：" + context;
    }
}
