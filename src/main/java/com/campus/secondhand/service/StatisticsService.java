package com.campus.secondhand.service;

import com.campus.secondhand.dao.StatisticsDao;
import com.campus.secondhand.model.ChartPoint;
import com.campus.secondhand.model.StatsSummary;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserOverview;
import com.campus.secondhand.model.UserRole;
import java.util.List;
import java.util.function.Consumer;

/**
 * 统计业务服务，提供后台运营看板所需的数据和摘要文案。
 */
public class StatisticsService {

	private final StatisticsDao statisticsDao;
	private final AiService aiService;
	private final AdminLogService adminLogService;

	/**
	 * 创建统计业务服务。
	 */
	public StatisticsService() {
		this.statisticsDao = new StatisticsDao();
		this.aiService = AiServiceFactory.create();
		this.adminLogService = new AdminLogService();
	}

	/**
	 * 加载平台统计汇总。
	 */
	public StatsSummary loadSummary() {
		// 首页统计卡片和运营摘要共用该汇总数据。
		return statisticsDao.loadSummary();
	}

	/**
	 * 查询用户概览列表。
	 */
	public List<UserOverview> listUserOverviews(String keyword) {
		// 用户管理页展示发布数和成交数时使用该列表。
		return statisticsDao.listUserOverviews(keyword);
	}

	/**
	 * 查询商品分类统计。
	 */
	public List<ChartPoint> listGoodsCategoryStats() {
		// 分类分布图，反映平台商品结构。
		return statisticsDao.listGoodsCategoryStats();
	}

	/**
	 * 查询分类成交数量统计。
	 */
	public List<ChartPoint> listCategorySoldCountStats() {
		return statisticsDao.listCategorySoldCountStats();
	}

	/**
	 * 查询分类成交金额统计。
	 */
	public List<ChartPoint> listCategoryRevenueStats() {
		return statisticsDao.listCategoryRevenueStats();
	}

	/**
	 * 查询商品状态统计。
	 */
	public List<ChartPoint> listGoodsStatusStats() {
		// 商品状态图，用于看待审核/在售/已售的整体比例。
		return statisticsDao.listGoodsStatusStats();
	}

	/**
	 * 查询最近订单趋势。
	 */
	public List<ChartPoint> listRecentOrderTrend() {
		// 最近订单趋势图，通常用于展示交易活跃度变化。
		return statisticsDao.listRecentOrderTrend();
	}

	public String buildSummaryTextStreaming(Consumer<String> onDelta) {
		return aiService.buildOperationsSummaryStreaming(buildStatsContext(), onDelta);
	}

	/**
	 * 记录管理员触发的 AI 运营分析，并返回生成结果。
	 */
	public String buildSummaryTextStreaming(User admin, Consumer<String> onDelta) {
		ensureAdmin(admin);
		String result = buildSummaryTextStreaming(onDelta);
		recordAiLog(admin, "AI_OPERATIONS_SUMMARY", "调用 AI 运营分析");
		return result;
	}

	public String buildInterpretationTextStreaming(Consumer<String> onDelta) {
		return aiService.interpretStatisticsStreaming(buildStatsContext(), onDelta);
	}

	/**
	 * 记录管理员触发的 AI 统计图解读，并返回生成结果。
	 */
	public String buildInterpretationTextStreaming(User admin, Consumer<String> onDelta) {
		ensureAdmin(admin);
		String result = buildInterpretationTextStreaming(onDelta);
		recordAiLog(admin, "AI_STATS_INTERPRETATION", "调用 AI 统计图解读");
		return result;
	}

	private void ensureAdmin(User user) {
		if (user == null || user.getRole() != UserRole.ADMIN) {
			throw new BusinessException("只有管理员可以执行该操作");
		}
	}

	private void recordAiLog(User admin, String action, String detail) {
		// goal.md 要求管理员调用 AI 分析也要留下审计记录，便于操作日志页追踪。
		adminLogService.record(admin, action, "AI", null, detail);
	}

	private String buildStatsContext() {
		// 将关键指标拼成一段自然语言，供 AI 或 Mock 摘要使用。
		StatsSummary summary = loadSummary();
		return String.format(
				"当前共有 %d 位用户，其中正常 %d 位、封禁 %d 位；平台累计 %d 件商品，在售 %d 件、待审核 %d 件、已售 %d 件；累计订单 %d 笔，今日成交 %d 笔，成交额 %.2f 元。",
				summary.getTotalUsers(), summary.getNormalUsers(), summary.getBannedUsers(), summary.getTotalGoods(),
				summary.getOnSaleGoods(), summary.getPendingGoods(), summary.getSoldGoods(), summary.getTotalOrders(),
				summary.getTodayOrders(), summary.getTotalRevenue());
	}
}
