package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.GoodsCategory;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.ImagePreviewLoader;
import com.campus.secondhand.util.Session;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PublishGoodsController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private TextField titleField;

	@FXML
	private ChoiceBox<String> categoryBox;

	@FXML
	private TextField originalPriceField;

	@FXML
	private TextField currentPriceField;

	@FXML
	private TextField conditionField;

	@FXML
	private TextArea descriptionArea;

	@FXML
	private TextArea aiSuggestionArea;

	@FXML
	private Label imageSummaryLabel;

	@FXML
	private ListView<String> imageListView;

	@FXML
	private ImageView imagePreviewView;

	private final GoodsService goodsService = new GoodsService();
	private final List<Path> selectedImages = new ArrayList<Path>();
	private int primaryImageIndex = -1;

	@FXML
	private void initialize() {
		if (categoryBox != null) {
			categoryBox.setItems(FXCollections.observableArrayList(categories()));
			categoryBox.getSelectionModel().selectFirst();
		}
		if (conditionField != null) {
			conditionField.setText("8");
		}
		if (imageListView != null) {
			imageListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
				updatePreview(newValue == null ? -1 : newValue.intValue());
			});
		}
		updatePreview(-1);
		User currentUser = Session.getCurrentUser();
		if (currentUserLabel != null) {
			currentUserLabel.setText(currentUser == null ? "未登录" : "当前用户：" + currentUser.getUsername());
		}
	}

	@FXML
	private void handleOptimize() {
		if (descriptionArea == null) {
			return;
		}
		String suggestion = goodsService.optimizeDescription(descriptionArea.getText());
		if (aiSuggestionArea != null) {
			aiSuggestionArea.setText(suggestion);
		}
	}

	@FXML
	private void handleSubmit() {
		try {
			User currentUser = Session.getCurrentUser();
			if (currentUser == null) {
				SceneManager.show("login.fxml", AppConfig.getAppTitle());
				return;
			}
			com.campus.secondhand.model.Goods goods = goodsService.publishGoods(currentUser, getText(titleField),
					getText(categoryBox), parseDouble(getText(originalPriceField), "原价不能为空"),
					parseDouble(getText(currentPriceField), "现价不能为空"), parseInt(getText(conditionField), "新旧程度不能为空"),
					getText(descriptionArea));
			goodsService.saveGoodsImages(currentUser, goods.getId(), selectedImages, primaryImageIndex);
			AlertUtil.showInfo("提交成功", "商品已进入待审核状态。");
			SceneManager.show("home.fxml", AppConfig.getAppTitle());
		} catch (BusinessException ex) {
			AlertUtil.showWarning("提交失败", ex.getMessage());
		}
	}

	@FXML
	private void handleAddImages() {
		if (selectedImages.size() >= 3) {
			AlertUtil.showWarning("添加失败", "商品图片最多上传 3 张。");
			return;
		}
		FileChooser chooser = new FileChooser();
		chooser.setTitle("选择商品图片");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg"));
		Stage stage = SceneManager.getPrimaryStage();
		List<java.io.File> files = chooser.showOpenMultipleDialog(stage);
		if (files == null || files.isEmpty()) {
			return;
		}
		for (java.io.File file : files) {
			if (selectedImages.size() >= 3) {
				break;
			}
			selectedImages.add(file.toPath());
		}
		if (primaryImageIndex < 0 && !selectedImages.isEmpty()) {
			primaryImageIndex = 0;
		}
		refreshImageList();
	}

	@FXML
	private void handleRemoveImage() {
		int selectedIndex = imageListView == null ? -1 : imageListView.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0 || selectedIndex >= selectedImages.size()) {
			AlertUtil.showWarning("提示", "请先选择一张图片。");
			return;
		}
		selectedImages.remove(selectedIndex);
		if (selectedImages.isEmpty()) {
			primaryImageIndex = -1;
		} else if (selectedIndex == primaryImageIndex) {
			primaryImageIndex = 0;
		} else if (selectedIndex < primaryImageIndex) {
			primaryImageIndex--;
		}
		refreshImageList();
	}

	@FXML
	private void handleSetPrimaryImage() {
		int selectedIndex = imageListView == null ? -1 : imageListView.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0 || selectedIndex >= selectedImages.size()) {
			AlertUtil.showWarning("提示", "请先选择一张图片。");
			return;
		}
		primaryImageIndex = selectedIndex;
		refreshImageList();
	}

	@FXML
	private void handleBack() {
		SceneManager.show("home.fxml", AppConfig.getAppTitle());
	}

	private List<String> categories() {
		return FXCollections.observableArrayList(GoodsCategory.TEXTBOOK.getDisplayName(),
				GoodsCategory.DIGITAL.getDisplayName(), GoodsCategory.CLOTHING.getDisplayName(),
				GoodsCategory.DAILY.getDisplayName(), GoodsCategory.OTHER.getDisplayName());
	}

	private String getText(TextField field) {
		return field == null ? null : field.getText();
	}

	private String getText(TextArea field) {
		return field == null ? null : field.getText();
	}

	private String getText(ChoiceBox<String> field) {
		return field == null ? null : field.getSelectionModel().getSelectedItem();
	}

	private double parseDouble(String value, String message) {
		if (value == null || value.trim().isEmpty()) {
			throw new BusinessException(message);
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			throw new BusinessException(message);
		}
	}

	private int parseInt(String value, String message) {
		if (value == null || value.trim().isEmpty()) {
			throw new BusinessException(message);
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			throw new BusinessException(message);
		}
	}

	private void refreshImageList() {
		List<String> items = new ArrayList<String>();
		for (int i = 0; i < selectedImages.size(); i++) {
			String prefix = i == primaryImageIndex ? "[主图] " : "";
			items.add(prefix + selectedImages.get(i).getFileName().toString());
		}
		if (imageListView != null) {
			imageListView.setItems(FXCollections.observableArrayList(items));
		}
		if (imageSummaryLabel != null) {
			imageSummaryLabel.setText("已选择 " + selectedImages.size() + " 张图片。");
		}
		updatePreview(imageListView == null ? -1 : imageListView.getSelectionModel().getSelectedIndex());
	}

	private void updatePreview(int selectedIndex) {
		if (imagePreviewView == null) {
			return;
		}
		if (selectedIndex < 0 || selectedIndex >= selectedImages.size()) {
			imagePreviewView.setImage(ImagePreviewLoader.loadHideImage());
			return;
		}
		imagePreviewView.setImage(ImagePreviewLoader.loadFromPath(selectedImages.get(selectedIndex)));
	}
}
