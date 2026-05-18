package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private TextField phoneField;

	@FXML
	private Label hintLabel;

	private final UserService userService = new UserService();

	@FXML
	private void initialize() {
		if (hintLabel != null) {
			hintLabel.setText("注册后默认角色为普通用户，状态为正常。");
		}
	}

	@FXML
	private void handleRegister() {
		try {
			userService.register(getText(usernameField), getText(passwordField), getText(phoneField));
			AlertUtil.showInfo("注册成功", "请使用新账号登录。");
			SceneManager.show("login.fxml", AppConfig.getAppTitle());
		} catch (BusinessException ex) {
			setHint(ex.getMessage());
		}
	}

	@FXML
	private void handleBackToLogin() {
		SceneManager.show("login.fxml", AppConfig.getAppTitle());
	}

	private String getText(TextField field) {
		return field == null ? null : field.getText();
	}

	private void setHint(String message) {
		if (hintLabel != null) {
			hintLabel.setText(message);
		}
	}
}
