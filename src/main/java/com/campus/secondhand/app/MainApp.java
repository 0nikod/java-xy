package com.campus.secondhand.app;

import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.config.DatabaseInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) {
		DatabaseInitializer.initialize();
		SceneManager.initialize(primaryStage);
		SceneManager.show("login.fxml", AppConfig.getAppTitle());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
