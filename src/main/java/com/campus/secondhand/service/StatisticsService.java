package com.campus.secondhand.service;

import com.campus.secondhand.dao.StatisticsDao;
import com.campus.secondhand.model.ChartPoint;
import com.campus.secondhand.model.StatsSummary;
import com.campus.secondhand.model.UserOverview;
import java.util.List;

/**
 * 统计业务服务，提供后台运营看板所需的数据和摘要文案。
 */
public class StatisticsService {

    private final StatisticsDao statisticsDao;
    private final AiService aiService;

    /**
     * 创建统计业务服务。
     */
    public StatisticsService() {
        this.statisticsDao = new StatisticsDao();
        this.aiService = AiServiceFactory.create();
    }

    /**
     * 加载平台统计汇总。
     */
    public StatsSummary loadSummary() {
        return statisticsDao.loadSummary();
    }

    /**
     * 查询用户概览列表。
     */
    public List<UserOverview> listUserOverviews(String keyword) {
        return statisticsDao.listUserOverviews(keyword);
    }

    /**
     * 查询商品分类统计。
     */
    public List<ChartPoint> listGoodsCategoryStats() {
        return statisticsDao.listGoodsCategoryStats();
    }

    /**
     * 查询商品状态统计。
     */
    public List<ChartPoint> listGoodsStatusStats() {
        return statisticsDao.listGoodsStatusStats();
    }

    /**
     * 查询最近订单趋势。
     */
    public List<ChartPoint> listRecentOrderTrend() {
        return statisticsDao.listRecentOrderTrend();
    }

    /**
     * 生成可直接展示在页面上的运营摘要文本。
     */
    public String buildSummaryText() {
        return aiService.buildOperationsSummary(buildStatsContext());
    }

    /**
     * 生成可直接展示在页面上的图表解读文本。
     */
    public String buildInterpretationText() {
        StatsSummary summary = loadSummary();
        return String.format(
                "Mock 图表解读：当前商品总量 %d 件，已成交 %d 件，说明基础交易链路已形成；待审核 %d 件，表示后台审核仍在发挥闸门作用；若封禁用户数为 %d，应继续关注异常账号活跃情况。",
                summary.getTotalGoods(),
                summary.getSoldGoods(),
                summary.getPendingGoods(),
                summary.getBannedUsers());
    }

    private String buildStatsContext() {
        StatsSummary summary = loadSummary();
        return String.format(
                "当前共有 %d 位用户，其中正常 %d 位、封禁 %d 位；平台累计 %d 件商品，在售 %d 件、待审核 %d 件、已售 %d 件；累计订单 %d 笔，成交额 %.2f 元。",
                summary.getTotalUsers(),
                summary.getNormalUsers(),
                summary.getBannedUsers(),
                summary.getTotalGoods(),
                summary.getOnSaleGoods(),
                summary.getPendingGoods(),
                summary.getSoldGoods(),
                summary.getTotalOrders(),
                summary.getTotalRevenue());
    }
}
