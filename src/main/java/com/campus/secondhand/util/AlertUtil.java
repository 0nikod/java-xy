package com.campus.secondhand.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public final class AlertUtil {

	private AlertUtil() {
	}

	public static void showInfo(String title, String content) {
		show(AlertType.INFORMATION, title, content);
	}

	public static void showWarning(String title, String content) {
		show(AlertType.WARNING, title, content);
	}

	public static void showError(String title, String content) {
		show(AlertType.ERROR, title, content);
	}

	private static void show(AlertType alertType, String title, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
