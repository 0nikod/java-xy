package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.AiSearchAssistResult;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsCategory;
import com.campus.secondhand.model.GoodsSortOption;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.util.Session;
import com.campus.secondhand.util.ViewState;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserHomeController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private Label messageLabel;

	@FXML
	private TextField keywordField;

	@FXML
	private ChoiceBox<String> categoryBox;

	@FXML
	private ChoiceBox<GoodsSortOption> sortBox;

	@FXML
	private Button aiSearchAssistButton;

	@FXML
	private TableView<Goods> goodsTable;

	@FXML
	private TableColumn<Goods, String> titleColumn;

	@FXML
	private TableColumn<Goods, String> categoryColumn;

	@FXML
	private TableColumn<Goods, Double> priceColumn;

	@FXML
	private TableColumn<Goods, Integer> conditionColumn;

	@FXML
	private TableColumn<Goods, String> imageColumn;

	@FXML
	private TableColumn<Goods, String> sellerColumn;

	@FXML
	private TableColumn<Goods, String> createdAtColumn;

	@FXML
	private TableColumn<Goods, String> statusColumn;

	private final GoodsService goodsService = new GoodsService();

	@FXML
	private void initialize() {
		configureTable();
		configureFilters();
		refreshGoods();
		updateCurrentUser();
		if (keywordField != null) {
			keywordField.setOnAction(event -> handleSearch());
		}
	}

	@FXML
	private void handleSearch() {
		refreshGoods();
	}

	@FXML
	private void handleAiSearchAssist() {
		String input = keywordField == null ? null : keywordField.getText();
		setMessage("AI 正在分析搜索需求...");
		if (aiSearchAssistButton != null) {
			aiSearchAssistButton.setDisable(true);
		}
		Task<AiSearchAssistResult> task = new Task<AiSearchAssistResult>() {
			@Override
			protected AiSearchAssistResult call() {
				return goodsService.assistSearchToCriteria(input);
			}
		};
		task.setOnSucceeded(event -> {
			if (aiSearchAssistButton != null) {
				aiSearchAssistButton.setDisable(false);
			}
			applyAiSearchResult(task.getValue());
		});
		task.setOnFailed(event -> {
			if (aiSearchAssistButton != null) {
				aiSearchAssistButton.setDisable(false);
			}
			setMessage("AI 搜索辅助失败：" + aiErrorMessage(task.getException()));
		});
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	@FXML
	private void handleReset() {
		if (keywordField != null) {
			keywordField.clear();
		}
		if (categoryBox != null) {
			categoryBox.getSelectionModel().selectFirst();
		}
		if (sortBox != null) {
			sortBox.getSelectionModel().select(GoodsSortOption.LATEST);
		}
		refreshGoods();
	}

	@FXML
	private void handlePublish() {
		SceneManager.show("publish_goods.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleUserCenter() {
		SceneManager.show("user_center.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleCart() {
		User currentUser = Session.getCurrentUser();
		if (currentUser == null) {
			SceneManager.show("login.fxml", AppConfig.getAppTitle());
			return;
		}
		SceneManager.show("cart.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleDetail() {
		Goods selected = goodsTable == null ? null : goodsTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			setMessage("请先选择一个商品。");
			return;
		}
		ViewState.setSelectedGoods(selected);
		SceneManager.show("goods_detail.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleMyPublished() {
		User currentUser = Session.getCurrentUser();
		if (currentUser == null) {
			SceneManager.show("login.fxml", AppConfig.getAppTitle());
			return;
		}
		List<Goods> goods = goodsService.listUserPublishedGoods(currentUser);
		goodsTable.setItems(FXCollections.observableArrayList(goods));
		setMessage("已切换到我发布的商品列表。");
	}

	@FXML
	private void handleLogout() {
		Session.clear();
		ViewState.clearSelectedGoods();
		SceneManager.show("login.fxml", AppConfig.getAppTitle());
	}

	private void configureTable() {
		if (titleColumn != null) {
			titleColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("title"));
		}
		if (categoryColumn != null) {
			categoryColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("category"));
		}
		if (priceColumn != null) {
			priceColumn.setCellValueFactory(new PropertyValueFactory<Goods, Double>("currentPrice"));
			priceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Goods, Double>() {
				@Override
				protected void updateItem(Double item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setStyle("");
					} else {
						setText(String.format("¥%.2f", item));
						setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
					}
				}
			});
		}
		if (conditionColumn != null) {
			conditionColumn.setCellValueFactory(new PropertyValueFactory<Goods, Integer>("conditionLevel"));
		}
		if (imageColumn != null) {
			imageColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("primaryImageName"));
		}
		if (sellerColumn != null) {
			sellerColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("sellerUsername"));
		}
		if (createdAtColumn != null) {
			createdAtColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("createdAt"));
		}
		if (statusColumn != null) {
			statusColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("statusText"));
			statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Goods, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || item == null) {
						setText(null);
						setGraphic(null);
					} else {
						Label badge = new Label(item);
						switch (item) {
							case "在售": badge.getStyleClass().add("badge-active"); break;
							case "已售出": badge.getStyleClass().add("badge-sold"); break;
							case "待审核": badge.getStyleClass().add("badge-pending"); break;
							case "已下架": badge.getStyleClass().add("badge-offline"); break;
							default: badge.getStyleClass().add("badge-sold"); break;
						}
						setText(null);
						setGraphic(badge);
					}
				}
			});
		}
		goodsTable.setRowFactory(tv -> {
			TableRow<Goods> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					ViewState.setSelectedGoods(row.getItem());
					SceneManager.show("goods_detail.fxml", AppConfig.getAppTitle());
				}
			});
			return row;
		});
		if (goodsTable != null) {
			goodsTable.setPlaceholder(new Label("暂无商品"));
		}
	}

	private void applyAiSearchResult(AiSearchAssistResult result) {
		if (result == null) {
			setMessage("AI 未生成有效搜索条件。请尝试手动搜索。");
			return;
		}
		if (keywordField != null) {
			keywordField.setText(result.getKeyword() == null ? "" : result.getKeyword());
		}
		selectCategory(result.getCategory());
		if (sortBox != null) {
			sortBox.getSelectionModel()
					.select(result.getSortOption() == null ? GoodsSortOption.LATEST : result.getSortOption());
		}
		refreshGoods(result.getExplanation());
	}

	private void selectCategory(String category) {
		if (categoryBox == null) {
			return;
		}
		if (category == null || category.trim().isEmpty()) {
			categoryBox.getSelectionModel().selectFirst();
			return;
		}
		for (int i = 0; i < categoryBox.getItems().size(); i++) {
			if (category.equals(categoryBox.getItems().get(i))) {
				categoryBox.getSelectionModel().select(i);
				return;
			}
		}
		categoryBox.getSelectionModel().selectFirst();
	}

	private void configureFilters() {
		if (categoryBox != null) {
			categoryBox.setItems(FXCollections.observableArrayList("全部", GoodsCategory.TEXTBOOK.getDisplayName(),
					GoodsCategory.DIGITAL.getDisplayName(), GoodsCategory.CLOTHING.getDisplayName(),
					GoodsCategory.DAILY.getDisplayName(), GoodsCategory.OTHER.getDisplayName()));
			categoryBox.getSelectionModel().selectFirst();
		}
		if (sortBox != null) {
			sortBox.setItems(FXCollections.observableArrayList(GoodsSortOption.values()));
			sortBox.getSelectionModel().select(GoodsSortOption.LATEST);
		}
	}

	private void refreshGoods() {
		refreshGoods("已加载可见商品。");
	}

	private void refreshGoods(String successMessage) {
		try {
			String keyword = keywordField == null ? null : keywordField.getText();
			String category = null;
			if (categoryBox != null && categoryBox.getSelectionModel().getSelectedIndex() > 0) {
				category = categoryBox.getSelectionModel().getSelectedItem();
			}
			GoodsSortOption sortOption = sortBox == null
					? GoodsSortOption.LATEST
					: sortBox.getSelectionModel().getSelectedItem();
			goodsTable.setItems(
					FXCollections.observableArrayList(goodsService.listPublicGoods(keyword, category, sortOption)));
			setMessage(successMessage == null || successMessage.trim().isEmpty() ? "已加载可见商品。" : successMessage);
		} catch (BusinessException ex) {
			setMessage(ex.getMessage());
		}
	}

	private void updateCurrentUser() {
		User currentUser = Session.getCurrentUser();
		if (currentUserLabel != null) {
			currentUserLabel.setText(currentUser == null ? "未登录" : "当前用户：" + currentUser.getUsername());
		}
	}

	private String aiErrorMessage(Throwable throwable) {
		Throwable cause = throwable == null ? null : throwable;
		while (cause != null && cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause == null || cause.getMessage() == null ? "AI 调用失败" : cause.getMessage();
	}

	private void setMessage(String text) {
		if (messageLabel != null) {
			messageLabel.setText(text);
		}
	}
}
