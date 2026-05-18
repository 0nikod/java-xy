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
    private Label aiInterpretationLabel;

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
        User currentUser = Session.getCurrentUser();
        if (currentUserLabel != null) {
            currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
        }
        configureTable();
        refreshStats();
    }

    @FXML
    private void handleRefresh() {
        refreshStats();
    }

    @FXML
    private void handleBack() {
        SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
    }

    private void configureTable() {
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
        if (aiInterpretationLabel != null) {
            aiInterpretationLabel.setText(statisticsService.buildInterpretationText());
        }
        populateCategoryChart();
        populateStatusChart();
        populateTrendChart();
        if (detailTable != null) {
            detailTable.setItems(FXCollections.observableArrayList(statisticsService.listUserOverviews(null)));
        }
    }

    private void populateCategoryChart() {
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
        if (statusBarChart == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("商品状态");
        for (ChartPoint point : statisticsService.listGoodsStatusStats()) {
            series.getData().add(new XYChart.Data<String, Number>(point.getLabel(), point.getValue()));
        }
        statusBarChart.getData().setAll(series);
    }

    private void populateTrendChart() {
        if (trendLineChart == null) {
            return;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("最近成交");
        for (ChartPoint point : statisticsService.listRecentOrderTrend()) {
            series.getData().add(new XYChart.Data<String, Number>(point.getLabel(), point.getValue()));
        }
        trendLineChart.getData().setAll(series);
    }
}
