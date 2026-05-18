package com.campus.secondhand.app;

import java.io.IOException;
import java.net.URL;

import com.campus.secondhand.util.AlertUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneManager {

	private static final double DEFAULT_WIDTH = 960.0;
	private static final double DEFAULT_HEIGHT = 640.0;
	private static final String STYLE_PATH = "/css/app.css";
	private static Stage primaryStage;

	private SceneManager() {
	}

	public static void initialize(Stage stage) {
		primaryStage = stage;
		primaryStage.setMinWidth(DEFAULT_WIDTH);
		primaryStage.setMinHeight(DEFAULT_HEIGHT);
	}

	public static void show(String fxmlName, String title) {
		ensureInitialized();
		try {
			URL resource = SceneManager.class.getResource("/fxml/" + fxmlName);
			if (resource == null) {
				throw new IllegalStateException("找不到页面资源: " + fxmlName);
			}
			Parent root = FXMLLoader.load(resource);
			Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
			URL stylesheet = SceneManager.class.getResource(STYLE_PATH);
			if (stylesheet != null) {
				scene.getStylesheets().add(stylesheet.toExternalForm());
			}
			primaryStage.setTitle(title);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			AlertUtil.showError("页面加载失败", e.getMessage());
			throw new IllegalStateException("无法加载页面: " + fxmlName, e);
		}
	}

	public static Stage getPrimaryStage() {
		ensureInitialized();
		return primaryStage;
	}

	private static void ensureInitialized() {
		if (primaryStage == null) {
			throw new IllegalStateException("SceneManager 尚未初始化");
		}
	}
}
