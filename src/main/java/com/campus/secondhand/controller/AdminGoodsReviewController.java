package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.Session;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private TextArea rejectReasonArea;

    private final GoodsService goodsService = new GoodsService();

    @FXML
    private void initialize() {
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
    private void handleReject() {
        Goods selected = getSelectedGoods();
        if (selected == null) {
            AlertUtil.showWarning("提示", "请先选择一个待审核商品。");
            return;
        }
        try {
            goodsService.rejectGoods(Session.getCurrentUser(), selected.getId(), getRejectReason());
            AlertUtil.showInfo("已驳回", "商品已驳回并写入原因。");
            if (rejectReasonArea != null) {
                rejectReasonArea.clear();
            }
            refreshPendingGoods();
        } catch (BusinessException ex) {
            AlertUtil.showWarning("驳回失败", ex.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
    }

    private void configureTable() {
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
        if (goods == null) {
            if (detailLabel != null) {
                detailLabel.setText("请选择一个待审核商品查看详情。");
            }
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("名称：").append(goods.getTitle()).append('\n');
        builder.append("分类：").append(goods.getCategory()).append('\n');
        builder.append("卖家：").append(goods.getSellerUsername()).append('\n');
        builder.append("现价：").append(goods.getCurrentPrice()).append('\n');
        builder.append("成色：").append(goods.getConditionLevel()).append('\n');
        builder.append("描述：").append(goods.getDescription());
        if (detailLabel != null) {
            detailLabel.setText(builder.toString());
        }
    }

    private Goods getSelectedGoods() {
        return pendingTable == null ? null : pendingTable.getSelectionModel().getSelectedItem();
    }

    private String getRejectReason() {
        return rejectReasonArea == null ? null : rejectReasonArea.getText();
    }

    private void updateUserLabel() {
        User currentUser = Session.getCurrentUser();
        if (currentUserLabel != null) {
            currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
        }
    }
}
