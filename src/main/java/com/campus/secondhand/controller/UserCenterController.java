package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.Review;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.service.OrderService;
import com.campus.secondhand.service.ReviewService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.Session;
import com.campus.secondhand.util.ViewState;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UserCenterController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private Label summaryLabel;

	@FXML
	private TableView<Goods> publishedGoodsTable;

	@FXML
	private TableColumn<Goods, String> publishedTitleColumn;

	@FXML
	private TableColumn<Goods, String> publishedCategoryColumn;

	@FXML
	private TableColumn<Goods, Double> publishedPriceColumn;

	@FXML
	private TableColumn<Goods, String> publishedStatusColumn;

	@FXML
	private TableView<Order> purchasedOrdersTable;

	@FXML
	private TableColumn<Order, String> purchasedOrderNoColumn;

	@FXML
	private TableColumn<Order, String> purchasedGoodsColumn;

	@FXML
	private TableColumn<Order, Double> purchasedPriceColumn;

	@FXML
	private TableColumn<Order, String> purchasedSellerColumn;

	@FXML
	private TableView<Order> soldOrdersTable;

	@FXML
	private TableColumn<Order, String> soldOrderNoColumn;

	@FXML
	private TableColumn<Order, String> soldGoodsColumn;

	@FXML
	private TableColumn<Order, Double> soldPriceColumn;

	@FXML
	private TableColumn<Order, String> soldBuyerColumn;

	@FXML
	private TableView<Review> publishedReviewsTable;

	@FXML
	private TableColumn<Review, String> publishedReviewGoodsColumn;

	@FXML
	private TableColumn<Review, String> publishedReviewRatingColumn;

	@FXML
	private TableColumn<Review, String> publishedReviewContentColumn;

	@FXML
	private TableView<Review> receivedReviewsTable;

	@FXML
	private TableColumn<Review, String> receivedReviewGoodsColumn;

	@FXML
	private TableColumn<Review, String> receivedReviewRatingColumn;

	@FXML
	private TableColumn<Review, String> receivedReviewContentColumn;

	private final GoodsService goodsService = new GoodsService();
	private final OrderService orderService = new OrderService();
	private final ReviewService reviewService = new ReviewService();

	@FXML
	private void initialize() {
		configureTables();
		refreshAll();
	}

	@FXML
	private void handleRefresh() {
		refreshAll();
	}

	@FXML
	private void handleOffShelf() {
		Goods selectedGoods = publishedGoodsTable == null
				? null
				: publishedGoodsTable.getSelectionModel().getSelectedItem();
		if (selectedGoods == null) {
			AlertUtil.showWarning("提示", "请先选择要下架的商品。");
			return;
		}
		try {
			goodsService.offShelfGoods(currentUser(), selectedGoods.getId());
			AlertUtil.showInfo("操作成功", "商品已下架。");
			refreshAll();
		} catch (BusinessException ex) {
			AlertUtil.showWarning("下架失败", ex.getMessage());
		}
	}

	@FXML
	private void handleBack() {
		SceneManager.show("home.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleCreateReview() {
		Order selectedOrder = purchasedOrdersTable == null
				? null
				: purchasedOrdersTable.getSelectionModel().getSelectedItem();
		if (selectedOrder == null) {
			AlertUtil.showWarning("提示", "请先选择要评价的购买订单。");
			return;
		}
		ViewState.setSelectedOrder(selectedOrder);
		showReviewDialog();
		ViewState.clearSelectedOrder();
		refreshAll();
	}

	private void configureTables() {
		if (publishedTitleColumn != null) {
			publishedTitleColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("title"));
		}
		if (publishedCategoryColumn != null) {
			publishedCategoryColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("category"));
		}
		if (publishedPriceColumn != null) {
			publishedPriceColumn.setCellValueFactory(new PropertyValueFactory<Goods, Double>("currentPrice"));
		}
		if (publishedStatusColumn != null) {
			publishedStatusColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("statusText"));
		}
		if (purchasedOrderNoColumn != null) {
			purchasedOrderNoColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("orderNo"));
		}
		if (purchasedGoodsColumn != null) {
			purchasedGoodsColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("goodsTitle"));
		}
		if (purchasedPriceColumn != null) {
			purchasedPriceColumn.setCellValueFactory(new PropertyValueFactory<Order, Double>("dealPrice"));
		}
		if (purchasedSellerColumn != null) {
			purchasedSellerColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("sellerUsername"));
		}
		if (soldOrderNoColumn != null) {
			soldOrderNoColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("orderNo"));
		}
		if (soldGoodsColumn != null) {
			soldGoodsColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("goodsTitle"));
		}
		if (soldPriceColumn != null) {
			soldPriceColumn.setCellValueFactory(new PropertyValueFactory<Order, Double>("dealPrice"));
		}
		if (soldBuyerColumn != null) {
			soldBuyerColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("buyerUsername"));
		}
		if (publishedReviewGoodsColumn != null) {
			publishedReviewGoodsColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("goodsTitle"));
		}
		if (publishedReviewRatingColumn != null) {
			publishedReviewRatingColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("ratingText"));
		}
		if (publishedReviewContentColumn != null) {
			publishedReviewContentColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("content"));
		}
		if (receivedReviewGoodsColumn != null) {
			receivedReviewGoodsColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("goodsTitle"));
		}
		if (receivedReviewRatingColumn != null) {
			receivedReviewRatingColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("ratingText"));
		}
		if (receivedReviewContentColumn != null) {
			receivedReviewContentColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("content"));
		}
	}

	private void refreshAll() {
		User currentUser = currentUser();
		if (currentUserLabel != null) {
			currentUserLabel.setText("当前用户：" + currentUser.getUsername());
		}

		List<Goods> publishedGoods = goodsService.listUserPublishedGoods(currentUser);
		List<Order> purchasedOrders = orderService.listPurchasedOrders(currentUser);
		List<Order> soldOrders = orderService.listSoldOrders(currentUser);
		List<Review> publishedReviews = reviewService.listPublishedReviews(currentUser);
		List<Review> receivedReviews = reviewService.listReceivedReviews(currentUser);

		if (publishedGoodsTable != null) {
			publishedGoodsTable.setItems(FXCollections.observableArrayList(publishedGoods));
		}
		if (purchasedOrdersTable != null) {
			purchasedOrdersTable.setItems(FXCollections.observableArrayList(purchasedOrders));
		}
		if (soldOrdersTable != null) {
			soldOrdersTable.setItems(FXCollections.observableArrayList(soldOrders));
		}
		if (publishedReviewsTable != null) {
			publishedReviewsTable.setItems(FXCollections.observableArrayList(publishedReviews));
		}
		if (receivedReviewsTable != null) {
			receivedReviewsTable.setItems(FXCollections.observableArrayList(receivedReviews));
		}
		if (summaryLabel != null) {
			summaryLabel.setText(String.format("我发布 %d 件商品，买入 %d 笔订单，卖出 %d 笔订单。", publishedGoods.size(),
					purchasedOrders.size(), soldOrders.size()));
		}
	}

	private void showReviewDialog() {
		try {
			URL resource = getClass().getResource("/fxml/review_dialog.fxml");
			if (resource == null) {
				throw new IllegalStateException("找不到评价窗口资源");
			}
			Parent root = FXMLLoader.load(resource);
			Stage dialog = new Stage();
			dialog.setTitle("发布评价");
			dialog.initOwner(SceneManager.getPrimaryStage());
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.setScene(new Scene(root));
			dialog.showAndWait();
		} catch (IOException e) {
			throw new IllegalStateException("无法打开评价窗口", e);
		}
	}

	private User currentUser() {
		User user = Session.getCurrentUser();
		if (user == null) {
			throw new BusinessException("请先登录");
		}
		return user;
	}
}
