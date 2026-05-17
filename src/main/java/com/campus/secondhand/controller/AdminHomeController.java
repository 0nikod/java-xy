package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminHomeController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label summaryLabel;

    private final GoodsService goodsService = new GoodsService();

    @FXML
    private void initialize() {
        User currentUser = Session.getCurrentUser();
        if (currentUserLabel != null) {
            currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
        }
        if (summaryLabel != null) {
            summaryLabel.setText(goodsService.buildOperationsSummary());
        }
    }

    @FXML
    private void handleReviewGoods() {
        SceneManager.show("admin_review.fxml", AppConfig.getAppTitle());
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        SceneManager.show("login.fxml", AppConfig.getAppTitle());
    }
}
