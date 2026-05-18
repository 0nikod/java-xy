package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserRole;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.util.Session;
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

	private final UserService userService = new UserService();

	@FXML
	private void initialize() {
		if (hintLabel != null) {
			hintLabel.setText("使用 admin/admin123 登录管理员后台，使用 demo_user/user123 登录普通用户首页，使用 demo_buyer/buyer123 登录买家订单页。");
		}
	}

	@FXML
	private void handleLogin() {
		try {
			User user = userService.login(text(usernameField), text(passwordField));
			Session.setCurrentUser(user);
			if (user.getRole() == UserRole.ADMIN) {
				SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
			} else {
				SceneManager.show("home.fxml", AppConfig.getAppTitle());
			}
		} catch (BusinessException ex) {
			if (hintLabel != null) {
				hintLabel.setText(ex.getMessage());
			}
		}
	}

	@FXML
	private void handleRegister() {
		SceneManager.show("register.fxml", AppConfig.getAppTitle());
	}

	private String text(TextField field) {
		return field == null ? null : field.getText();
	}
}
