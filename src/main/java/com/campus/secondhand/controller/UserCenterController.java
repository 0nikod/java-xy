package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.service.OrderService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.Session;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

	private final GoodsService goodsService = new GoodsService();
	private final OrderService orderService = new OrderService();

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
	private void handleReviewPlaceholder() {
		AlertUtil.showInfo("后续开放", "我的评价与收到的评价将在后续扩展中开放。");
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
	}

	private void refreshAll() {
		User currentUser = currentUser();
		if (currentUserLabel != null) {
			currentUserLabel.setText("当前用户：" + currentUser.getUsername());
		}

		List<Goods> publishedGoods = goodsService.listUserPublishedGoods(currentUser);
		List<Order> purchasedOrders = orderService.listPurchasedOrders(currentUser);
		List<Order> soldOrders = orderService.listSoldOrders(currentUser);

		if (publishedGoodsTable != null) {
			publishedGoodsTable.setItems(FXCollections.observableArrayList(publishedGoods));
		}
		if (purchasedOrdersTable != null) {
			purchasedOrdersTable.setItems(FXCollections.observableArrayList(purchasedOrders));
		}
		if (soldOrdersTable != null) {
			soldOrdersTable.setItems(FXCollections.observableArrayList(soldOrders));
		}
		if (summaryLabel != null) {
			summaryLabel.setText(String.format("我发布 %d 件商品，买入 %d 笔订单，卖出 %d 笔订单。", publishedGoods.size(),
					purchasedOrders.size(), soldOrders.size()));
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
