package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsImage;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.ImagePreviewLoader;
import com.campus.secondhand.util.Session;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * 管理员商品审核页控制器。
 * <p>
 * 负责把“待审核列表 → 详情展示 → 图片预览 → 审核/删除操作”串成完整交互流程。
 */
public class AdminGoodsReviewController {

	@FXML
	private Label currentUserLabel;

	@FXML
	private Label detailLabel;

	@FXML
	private TableView<Goods> pendingTable;

	@FXML
	private TableColumn<Goods, String> titleColumn;

	@FXML
	private TableColumn<Goods, String> sellerColumn;

	@FXML
	private TableColumn<Goods, String> categoryColumn;

	@FXML
	private TableColumn<Goods, Double> priceColumn;

	@FXML
	private TableColumn<Goods, String> statusColumn;

	@FXML
	private TextArea deleteReasonArea;

	@FXML
	private ListView<String> imageListView;

	@FXML
	private ImageView imagePreviewView;

	private final GoodsService goodsService = new GoodsService();

	@FXML
	private void initialize() {
		// 初始化表格列、加载待审核商品、显示当前登录管理员信息，并绑定列表选择事件。
		configureTable();
		refreshPendingGoods();
		updateUserLabel();
		if (pendingTable != null) {
			pendingTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Goods>() {
				@Override
				public void changed(ObservableValue<? extends Goods> observable, Goods oldValue, Goods newValue) {
					showDetail(newValue);
				}
			});
		}
	}

	@FXML
	private void handleApprove() {
		// 只有选中商品后才能执行审核通过，审核通过后会刷新待审核列表。
		Goods selected = getSelectedGoods();
		if (selected == null) {
			AlertUtil.showWarning("提示", "请先选择一个待审核商品。");
			return;
		}
		try {
			goodsService.approveGoods(Session.getCurrentUser(), selected.getId());
			AlertUtil.showInfo("审核通过", "商品已进入在售状态。");
			refreshPendingGoods();
		} catch (BusinessException ex) {
			AlertUtil.showWarning("审核失败", ex.getMessage());
		}
	}

	@FXML
	private void handleBack() {
		// 返回后台首页，避免停留在审核详情页。
		SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
	}

	@FXML
	private void handleDelete() {
		// 删除违规商品前先校验是否已选择商品，再把删除原因一并提交给服务层记录日志。
		Goods selected = getSelectedGoods();
		if (selected == null) {
			AlertUtil.showWarning("提示", "请先选择一个商品。");
			return;
		}
		try {
			goodsService.deleteGoods(Session.getCurrentUser(), selected.getId(), getDeleteReason());
			AlertUtil.showInfo("删除成功", "违规商品已删除并写入日志。");
			if (deleteReasonArea != null) {
				deleteReasonArea.clear();
			}
			refreshPendingGoods();
		} catch (BusinessException ex) {
			AlertUtil.showWarning("删除失败", ex.getMessage());
		}
	}

	private void configureTable() {
		// 将表格列绑定到 Goods 属性，确保列表展示与实体字段一一对应。
		if (titleColumn != null) {
			titleColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("title"));
		}
		if (sellerColumn != null) {
			sellerColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("sellerUsername"));
		}
		if (categoryColumn != null) {
			categoryColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("category"));
		}
		if (priceColumn != null) {
			priceColumn.setCellValueFactory(new PropertyValueFactory<Goods, Double>("currentPrice"));
		}
		if (statusColumn != null) {
			statusColumn.setCellValueFactory(new PropertyValueFactory<Goods, String>("statusText"));
		}
	}

	private void refreshPendingGoods() {
		// 刷新待审核列表，同时在顶部显示当前数量，空列表时清空详情区。
		List<Goods> goods = goodsService.listPendingGoods();
		if (pendingTable != null) {
			pendingTable.setItems(FXCollections.observableArrayList(goods));
		}
		if (detailLabel != null) {
			detailLabel.setText("待审核商品数量：" + goods.size());
		}
		if (goods.isEmpty()) {
			showDetail(null);
		}
	}

	private void showDetail(Goods goods) {
		// 根据表格选择项展示商品详情，并同步加载图片列表与主图预览。
		if (goods == null) {
			if (detailLabel != null) {
				detailLabel.setText("请选择一个待审核商品查看详情。");
			}
			if (imageListView != null) {
				imageListView.setItems(FXCollections.observableArrayList(new java.util.ArrayList<String>()));
			}
			updatePreview(new java.util.ArrayList<GoodsImage>(), -1);
			return;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("名称：").append(goods.getTitle()).append('\n');
		builder.append("分类：").append(goods.getCategory()).append('\n');
		builder.append("卖家：").append(goods.getSellerUsername()).append('\n');
		builder.append("现价：").append(goods.getCurrentPrice()).append('\n');
		builder.append("成色：").append(goods.getConditionText()).append('\n');
		builder.append("描述：").append(goods.getDescription()).append('\n');
		builder.append("主图：").append(goods.getPrimaryImagePath() == null ? "无" : goods.getPrimaryImagePath());
		if (detailLabel != null) {
			detailLabel.setText(builder.toString());
		}
		List<String> imageItems = new java.util.ArrayList<String>();
		for (GoodsImage image : goodsService.listGoodsImages(goods.getId())) {
			imageItems.add((image.isPrimary() ? "[主图] " : "") + image.getImagePath());
		}
		if (imageListView != null) {
			imageListView.setItems(FXCollections.observableArrayList(imageItems));
			imageListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
				updatePreview(goodsService.listGoodsImages(goods.getId()), newValue == null ? -1 : newValue.intValue());
			});
			if (!imageItems.isEmpty()) {
				imageListView.getSelectionModel().select(0);
				updatePreview(goodsService.listGoodsImages(goods.getId()), 0);
			} else {
				updatePreview(new java.util.ArrayList<GoodsImage>(), -1);
			}
		}
	}

	private Goods getSelectedGoods() {
		// 统一从表格当前选择项获取审核目标。
		return pendingTable == null ? null : pendingTable.getSelectionModel().getSelectedItem();
	}

	private String getDeleteReason() {
		// 删除原因允许为空值由服务层校验，这里只负责读取输入内容。
		return deleteReasonArea == null ? null : deleteReasonArea.getText();
	}

	private void updateUserLabel() {
		// 显示当前管理员身份，便于确认操作归属。
		User currentUser = Session.getCurrentUser();
		if (currentUserLabel != null) {
			currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
		}
	}

	private void updatePreview(List<GoodsImage> images, int index) {
		// 根据图片列表选中项更新预览图；越界或空选择时清空预览。
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
