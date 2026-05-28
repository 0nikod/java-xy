package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.Review;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.ReviewService;
import com.campus.secondhand.util.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminReviewController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private TableView<Review> reviewTable;

	@FXML
	private TableColumn<Review, String> goodsColumn;

	@FXML
	private TableColumn<Review, String> reviewerColumn;

	@FXML
	private TableColumn<Review, String> sellerColumn;

	@FXML
	private TableColumn<Review, String> ratingColumn;

	@FXML
	private TableColumn<Review, String> contentColumn;

	@FXML
	private TableColumn<Review, String> createdAtColumn;

	private final ReviewService reviewService = new ReviewService();

	@FXML
	private void initialize() {
		User currentUser = Session.getCurrentUser();
		if (currentUserLabel != null) {
			currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
		}
		configureTable();
		refreshReviews();
	}

	@FXML
	private void handleRefresh() {
		refreshReviews();
	}

	@FXML
	private void handleBack() {
		SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
	}

	private void configureTable() {
		if (goodsColumn != null) {
			goodsColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("goodsTitle"));
		}
		if (reviewerColumn != null) {
			reviewerColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("reviewerUsername"));
		}
		if (sellerColumn != null) {
			sellerColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("sellerUsername"));
		}
		if (ratingColumn != null) {
			ratingColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("ratingText"));
		}
		if (contentColumn != null) {
			contentColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("content"));
		}
		if (createdAtColumn != null) {
			createdAtColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("createdAt"));
		}
	}

	private void refreshReviews() {
		User currentUser = Session.getCurrentUser();
		if (reviewTable != null) {
			reviewTable.setItems(FXCollections.observableArrayList(reviewService.listAllReviews(currentUser)));
		}
	}
}
