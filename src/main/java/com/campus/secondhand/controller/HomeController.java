package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label summaryLabel;

    @FXML
    private void initialize() {
        if (summaryLabel != null) {
            summaryLabel.setText("数据库已初始化，项目主窗口可以启动。");
        }
    }

    @FXML
    private void handleBackToLogin() {
        SceneManager.show("login.fxml", AppConfig.getAppTitle());
    }
}
