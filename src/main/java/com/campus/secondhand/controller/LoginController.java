package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label hintLabel;

    @FXML
    private void initialize() {
        if (hintLabel != null) {
            hintLabel.setText("第一阶段先完成项目骨架、数据库初始化和页面跳转。");
        }
    }

    @FXML
    private void handleLogin() {
        if (usernameField != null && passwordField != null) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if ((username == null || username.trim().isEmpty()) || (password == null || password.trim().isEmpty())) {
                if (hintLabel != null) {
                    hintLabel.setText("请输入用户名和密码。");
                }
                return;
            }
        }
        SceneManager.show("home.fxml", AppConfig.getAppTitle());
    }
}
