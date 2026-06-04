package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.StatisticsService;
import com.campus.secondhand.util.Session;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminHomeController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private Label summaryLabel;

	@FXML
	private Label statUsersLabel;

	@FXML
	private Label statGoodsLabel;

	@FXML
	private Label statPendingLabel;

	@FXML
	private Label statTodayLabel;

	private final StatisticsService statisticsService = new StatisticsService();

	@FXML
	private void initialize() {
		User currentUser = Session.getCurrentUser();
		if (currentUserLabel != null) {
			if (currentUser == null) {
				currentUserLabel.setText("未登录");
			} else {
				currentUserLabel.setText("当前管理员: " + currentUser.getUsername());
			}
		}
		if (summaryLabel != null) {
			summaryLabel.setText("点击 AI 管理分析 生成运营分析摘要。");
		}
		loadStatCards();
	}

	@FXML
	private void handleAiManagementAnalysis() {
		loadSummaryStreaming();
	}

	private void loadSummaryStreaming() {
		if (summaryLabel == null) {
			return;
		}
		summaryLabel.setText("");
		StringBuilder builder = new StringBuilder();
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() {
				statisticsService.buildSummaryTextStreaming(delta -> Platform.runLater(() -> {
					builder.append(delta);
					summaryLabel.setText(builder.toString());
				}));
				return null;
			}
		};
		task.setOnFailed(event -> summaryLabel.setText("AI 摘要生成失败"));
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
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
	private void handleViewReviews() {
		SceneManager.show("admin_reviews.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleLogout() {
		Session.clear();
		SceneManager.show("login.fxml", AppConfig.getAppTitle());
	}

	private void loadStatCards() {
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() {
				try {
					com.campus.secondhand.model.StatsSummary summary = statisticsService.loadSummary();
					Platform.runLater(() -> {
						if (statUsersLabel != null) {
							statUsersLabel.setText(String.valueOf(summary.getTotalUsers()));
						}
						if (statGoodsLabel != null) {
							statGoodsLabel.setText(String.valueOf(summary.getTotalGoods()));
						}
						if (statPendingLabel != null) {
							statPendingLabel.setText(String.valueOf(summary.getPendingGoods()));
						}
						if (statTodayLabel != null) {
							statTodayLabel.setText(String.valueOf(summary.getTodayOrders()));
						}
					});
				} catch (Exception ignored) {
				}
				return null;
			}
		};
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
}
