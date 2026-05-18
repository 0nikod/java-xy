package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.StatisticsService;
import com.campus.secondhand.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminHomeController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private Label summaryLabel;

	private final StatisticsService statisticsService = new StatisticsService();

	@FXML
	private void initialize() {
		User currentUser = Session.getCurrentUser();
		if (currentUserLabel != null) {
			currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
		}
		if (summaryLabel != null) {
			summaryLabel.setText(statisticsService.buildSummaryText());
		}
	}

	@FXML
	private void handleReviewGoods() {
		SceneManager.show("admin_review.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleManageUsers() {
		SceneManager.show("admin_users.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleViewStats() {
		SceneManager.show("admin_stats.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleViewLogs() {
		SceneManager.show("admin_logs.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleLogout() {
		Session.clear();
		SceneManager.show("login.fxml", AppConfig.getAppTitle());
	}
}
