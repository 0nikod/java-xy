package com.campus.secondhand.controller;

import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.ReviewService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.Session;
import com.campus.secondhand.util.ViewState;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ReviewDialogController {

	@FXML
	private Label orderLabel;

	@FXML
	private ChoiceBox<Integer> ratingBox;

	@FXML
	private TextArea contentArea;

	private final ReviewService reviewService = new ReviewService();
	private Order order;

	@FXML
	private void initialize() {
		order = ViewState.getSelectedOrder();
		if (ratingBox != null) {
			ratingBox.getItems().addAll(1, 2, 3, 4, 5);
			ratingBox.getSelectionModel().select(Integer.valueOf(5));
		}
		if (orderLabel != null && order != null) {
			orderLabel.setText("订单：" + order.getOrderNo() + " | 商品：" + order.getGoodsTitle());
		}
	}

	@FXML
	private void handleSubmit() {
		try {
			User user = Session.getCurrentUser();
			Integer rating = ratingBox == null ? null : ratingBox.getSelectionModel().getSelectedItem();
			if (rating == null) {
				throw new BusinessException("请选择评分");
			}
			reviewService.createReview(user, order.getId(), rating.intValue(), contentArea == null ? null : contentArea.getText());
			AlertUtil.showInfo("评价成功", "评价已发布。关闭窗口后请刷新个人中心。");
			closeWindow();
		} catch (BusinessException ex) {
			AlertUtil.showWarning("评价失败", ex.getMessage());
		}
	}

	@FXML
	private void handleCancel() {
		closeWindow();
	}

	private void closeWindow() {
		if (contentArea != null && contentArea.getScene() != null) {
			contentArea.getScene().getWindow().hide();
		}
	}
}
