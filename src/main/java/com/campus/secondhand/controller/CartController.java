package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.CartItem;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.CartService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.Session;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CartController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private Label messageLabel;

	@FXML
	private TableView<CartItem> cartTable;

	@FXML
	private TableColumn<CartItem, String> titleColumn;

	@FXML
	private TableColumn<CartItem, String> categoryColumn;

	@FXML
	private TableColumn<CartItem, Double> priceColumn;

	@FXML
	private TableColumn<CartItem, String> sellerColumn;

	@FXML
	private TableColumn<CartItem, String> statusColumn;

	@FXML
	private TableColumn<CartItem, String> imageColumn;

	@FXML
	private TableColumn<CartItem, String> createdAtColumn;

	private final CartService cartService = new CartService();

	@FXML
	private void initialize() {
		configureTable();
		refreshCart();
	}

	@FXML
	private void handleRefresh() {
		refreshCart();
	}

	@FXML
	private void handleRemove() {
		CartItem selected = cartTable == null ? null : cartTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			setMessage("请先选择要移除的购物车商品。");
			return;
		}
		try {
			cartService.removeFromCart(currentUser(), selected.getId());
			setMessage("已从购物车移除。");
			refreshCart();
		} catch (BusinessException ex) {
			AlertUtil.showWarning("移除失败", ex.getMessage());
		}
	}

	@FXML
	private void handleCheckoutSelected() {
		if (cartTable == null || cartTable.getSelectionModel().getSelectedItems().isEmpty()) {
			setMessage("请先选择要结算的商品。");
			return;
		}
		List<Long> ids = new ArrayList<Long>();
		for (CartItem item : cartTable.getSelectionModel().getSelectedItems()) {
			ids.add(item.getId());
		}
		checkout(ids);
	}

	@FXML
	private void handleCheckoutAll() {
		if (cartTable == null || cartTable.getItems().isEmpty()) {
			setMessage("购物车为空，无需结算。");
			return;
		}
		List<Long> ids = new ArrayList<Long>();
		for (CartItem item : cartTable.getItems()) {
			ids.add(item.getId());
		}
		checkout(ids);
	}

	@FXML
	private void handleBack() {
		SceneManager.show("home.fxml", AppConfig.getAppTitle());
	}

	private void checkout(List<Long> ids) {
		try {
			List<Order> orders = cartService.checkout(currentUser(), ids);
			AlertUtil.showInfo("结算成功", "已生成 " + orders.size() + " 笔订单。");
			refreshCart();
		} catch (BusinessException ex) {
			AlertUtil.showWarning("结算失败", ex.getMessage());
		}
	}

	private void configureTable() {
		if (cartTable != null) {
			cartTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
		}
		if (titleColumn != null) {
			titleColumn.setCellValueFactory(new PropertyValueFactory<CartItem, String>("goodsTitle"));
		}
		if (categoryColumn != null) {
			categoryColumn.setCellValueFactory(new PropertyValueFactory<CartItem, String>("category"));
		}
		if (priceColumn != null) {
			priceColumn.setCellValueFactory(new PropertyValueFactory<CartItem, Double>("currentPrice"));
		}
		if (sellerColumn != null) {
			sellerColumn.setCellValueFactory(new PropertyValueFactory<CartItem, String>("sellerUsername"));
		}
		if (statusColumn != null) {
			statusColumn.setCellValueFactory(new PropertyValueFactory<CartItem, String>("goodsStatusText"));
		}
		if (imageColumn != null) {
			imageColumn.setCellValueFactory(new PropertyValueFactory<CartItem, String>("primaryImageName"));
		}
		if (createdAtColumn != null) {
			createdAtColumn.setCellValueFactory(new PropertyValueFactory<CartItem, String>("createdAt"));
		}
	}

	private void refreshCart() {
		try {
			User user = currentUser();
			if (currentUserLabel != null) {
				currentUserLabel.setText("当前用户：" + user.getUsername());
			}
			List<CartItem> items = cartService.listCartItems(user);
			if (cartTable != null) {
				cartTable.setItems(FXCollections.observableArrayList(items));
			}
			setMessage("购物车共有 " + items.size() + " 件商品。");
		} catch (BusinessException ex) {
			AlertUtil.showWarning("提示", ex.getMessage());
			SceneManager.show("login.fxml", AppConfig.getAppTitle());
		}
	}

	private User currentUser() {
		User user = Session.getCurrentUser();
		if (user == null) {
			throw new BusinessException("请先登录");
		}
		return user;
	}

	private void setMessage(String message) {
		if (messageLabel != null) {
			messageLabel.setText(message);
		}
	}
}
