package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.ChartPoint;
import com.campus.secondhand.model.StatsSummary;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserOverview;
import com.campus.secondhand.service.StatisticsService;
import com.campus.secondhand.util.Session;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminStatsController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label statsCardLabel;

    @FXML
    private Label aiSummaryLabel;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    private BarChart<String, Number> statusBarChart;

    @FXML
    private LineChart<String, Number> trendLineChart;

    @FXML
    private TableView<UserOverview> detailTable;

    @FXML
    private TableColumn<UserOverview, String> detailUserColumn;

    @FXML
    private TableColumn<UserOverview, String> detailStatusColumn;

    @FXML
    private TableColumn<UserOverview, Integer> detailGoodsCountColumn;

    @FXML
    private TableColumn<UserOverview, Integer> detailSoldCountColumn;

    private final StatisticsService statisticsService = new StatisticsService();

    @FXML
    private void initialize() {
        // 初始化时绑定当前管理员、表格列和统计视图。
        User currentUser = Session.getCurrentUser();
        if (currentUserLabel != null) {
            currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
        }
        configureTable();
        refreshStats();
    }

    @FXML
    private void handleRefresh() {
        // 手动刷新统计，便于后台数据变化后重新查看最新结果。
        refreshStats();
    }

    @FXML
    private void handleBack() {
        // 返回后台首页，结束统计查看流程。
        SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
    }

    private void configureTable() {
        // 表格列与 UserOverview 字段绑定，展示用户概览指标。
        if (detailUserColumn != null) {
            detailUserColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, String>("username"));
        }
        if (detailStatusColumn != null) {
            detailStatusColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, String>("status"));
        }
        if (detailGoodsCountColumn != null) {
            detailGoodsCountColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, Integer>("publishedGoodsCount"));
        }
        if (detailSoldCountColumn != null) {
            detailSoldCountColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, Integer>("soldOrderCount"));
        }
    }

    private void refreshStats() {
        // 汇总卡片、AI 文案和三类图表共用同一批统计数据，确保口径一致。
        StatsSummary summary = statisticsService.loadSummary();
        if (statsCardLabel != null) {
            statsCardLabel.setText(String.format(
                    "用户总数：%d | 正常：%d | 封禁：%d | 商品总数：%d | 在售：%d | 待审核：%d | 已成交订单：%d | 成交额：%.2f 元",
                    summary.getTotalUsers(), summary.getNormalUsers(), summary.getBannedUsers(), summary.getTotalGoods(),
                    summary.getOnSaleGoods(), summary.getPendingGoods(), summary.getTotalOrders(), summary.getTotalRevenue()));
        }
        if (aiSummaryLabel != null) {
            aiSummaryLabel.setText(statisticsService.buildSummaryText());
        }
        populateCategoryChart();
        populateStatusChart();
        populateTrendChart();
        if (detailTable != null) {
            detailTable.setItems(FXCollections.observableArrayList(statisticsService.listUserOverviews(null)));
        }
    }

    private void populateCategoryChart() {
        // 分类饼图反映商品供给结构。
        if (categoryPieChart == null) {
            return;
        }
        List<ChartPoint> points = statisticsService.listGoodsCategoryStats();
        categoryPieChart.setData(FXCollections.observableArrayList());
        for (ChartPoint point : points) {
            categoryPieChart.getData().add(new PieChart.Data(point.getLabel(), point.getValue()));
        }
    }

    private void populateStatusChart() {
        // 状态柱状图用于观察待审核、在售、已售比例。
        if (statusBarChart == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("商品状态");
        for (ChartPoint point : statisticsService.listGoodsStatusStats()) {
            series.getData().add(new XYChart.Data<String, Number>(point.getLabel(), point.getValue()));
        }
        statusBarChart.getData().clear();
        statusBarChart.getData().add(series);
    }

    private void populateTrendChart() {
        // 趋势折线图展示最近成交变化，方便查看交易热度。
        if (trendLineChart == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("最近成交");
        for (ChartPoint point : statisticsService.listRecentOrderTrend()) {
            series.getData().add(new XYChart.Data<String, Number>(point.getLabel(), point.getValue()));
        }
        trendLineChart.getData().clear();
        trendLineChart.getData().add(series);
    }
}
