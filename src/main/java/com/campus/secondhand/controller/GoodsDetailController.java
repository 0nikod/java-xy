package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsImage;
import com.campus.secondhand.model.Review;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.CartService;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.service.OrderService;
import com.campus.secondhand.service.ReviewService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.ImagePreviewLoader;
import com.campus.secondhand.util.Session;
import com.campus.secondhand.util.ViewState;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

public class GoodsDetailController {

	@FXML
	private Label titleLabel;

	@FXML
	private Label categoryLabel;

	@FXML
	private Label priceLabel;

	@FXML
	private Label conditionLabel;

	@FXML
	private Label sellerLabel;

	@FXML
	private Label statusLabel;

	@FXML
	private Label createdAtLabel;

	@FXML
	private TextArea descriptionArea;

	@FXML
	private Button buyButton;

	@FXML
	private ListView<String> imageListView;

	@FXML
	private ListView<String> reviewListView;

	@FXML
	private ImageView imagePreviewView;

	private final GoodsService goodsService = new GoodsService();
	private final OrderService orderService = new OrderService();
	private final CartService cartService = new CartService();
	private final ReviewService reviewService = new ReviewService();

	private Goods goods;

	@FXML
	private void initialize() {
		goods = ViewState.getSelectedGoods();
		if (goods == null) {
			setMessage("未选择商品，正在返回首页。");
			SceneManager.show("home.fxml", AppConfig.getAppTitle());
			return;
		}
		refreshDetail();
		User currentUser = Session.getCurrentUser();
		if (buyButton != null && currentUser != null && goods.getSellerId().equals(currentUser.getId())) {
			buyButton.setDisable(true);
			buyButton.setText("不能购买自己的商品");
		}
	}

	@FXML
	private void handleBuy() {
		try {
			User currentUser = Session.getCurrentUser();
			if (currentUser == null) {
				SceneManager.show("login.fxml", AppConfig.getAppTitle());
				return;
			}
			orderService.purchaseGoods(currentUser, goods.getId());
			AlertUtil.showInfo("购买成功", "订单已生成，商品已标记为已售出。");
			SceneManager.show("home.fxml", AppConfig.getAppTitle());
		} catch (BusinessException ex) {
			AlertUtil.showWarning("无法购买", ex.getMessage());
		}
	}

	@FXML
	private void handleBack() {
		SceneManager.show("home.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleAddToCart() {
		try {
			User currentUser = Session.getCurrentUser();
			if (currentUser == null) {
				SceneManager.show("login.fxml", AppConfig.getAppTitle());
				return;
			}
			cartService.addToCart(currentUser, goods.getId());
			AlertUtil.showInfo("加入成功", "商品已加入购物车。");
		} catch (BusinessException ex) {
			AlertUtil.showWarning("无法加入购物车", ex.getMessage());
		}
	}

	@FXML
	private void handleRefreshReviews() {
		refreshReviews();
	}

	private void refreshDetail() {
		Goods latest = goodsService.getGoodsDetail(goods.getId());
		goods = latest;
		if (titleLabel != null) {
			titleLabel.setText(latest.getTitle());
		}
		if (categoryLabel != null) {
			categoryLabel.setText("分类：" + latest.getCategory());
		}
		if (priceLabel != null) {
			priceLabel.setText(String.format("原价：%.2f  现价：%.2f", latest.getOriginalPrice(), latest.getCurrentPrice()));
		}
		if (conditionLabel != null) {
			conditionLabel.setText("新旧程度：" + latest.getConditionLevel());
		}
		if (sellerLabel != null) {
			sellerLabel.setText("卖家：" + latest.getSellerUsername());
		}
		if (statusLabel != null) {
			statusLabel.setText("状态：" + latest.getStatusText());
		}
		if (createdAtLabel != null) {
			createdAtLabel.setText("发布时间：" + latest.getCreatedAt());
		}
		if (descriptionArea != null) {
			descriptionArea.setText(latest.getDescription());
		}
		List<String> imageItems = new ArrayList<String>();
		List<GoodsImage> images = goodsService.listGoodsImages(latest.getId());
		for (GoodsImage image : images) {
			imageItems.add((image.isPrimary() ? "[主图] " : "") + image.getImagePath());
		}
		if (imageListView != null) {
			imageListView.setItems(javafx.collections.FXCollections.observableArrayList(imageItems));
			imageListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
				updatePreview(images, newValue == null ? -1 : newValue.intValue());
			});
			if (!images.isEmpty()) {
				imageListView.getSelectionModel().select(0);
				updatePreview(images, 0);
			} else {
				updatePreview(images, -1);
			}
		}
		refreshReviews();
	}

	private void refreshReviews() {
		if (reviewListView == null || goods == null) {
			return;
		}
		List<String> items = new ArrayList<String>();
		for (Review review : reviewService.listGoodsReviews(goods.getId())) {
			items.add(String.format("%s：%d 星 - %s（%s）", review.getReviewerUsername(), review.getRating(),
					review.getContent(), review.getCreatedAt()));
		}
		if (items.isEmpty()) {
			items.add("暂无评价");
		}
		reviewListView.setItems(javafx.collections.FXCollections.observableArrayList(items));
	}

	private void setMessage(String message) {
		AlertUtil.showWarning("提示", message);
	}

	private void updatePreview(List<GoodsImage> images, int index) {
		if (imagePreviewView == null) {
			return;
		}
		if (index < 0 || index >= images.size()) {
			imagePreviewView.setImage(ImagePreviewLoader.loadHideImage());
			return;
		}
		imagePreviewView.setImage(ImagePreviewLoader.loadFromPath(images.get(index).getImagePath()));
	}
}
