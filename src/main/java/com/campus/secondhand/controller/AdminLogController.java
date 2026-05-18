package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.AdminLog;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.AdminLogService;
import com.campus.secondhand.util.Session;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminLogController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private TableView<AdminLog> logTable;

    @FXML
    private TableColumn<AdminLog, String> adminColumn;

    @FXML
    private TableColumn<AdminLog, String> actionColumn;

    @FXML
    private TableColumn<AdminLog, String> targetTypeColumn;

    @FXML
    private TableColumn<AdminLog, Long> targetIdColumn;

    @FXML
    private TableColumn<AdminLog, String> detailColumn;

    @FXML
    private TableColumn<AdminLog, String> createdAtColumn;

    private final AdminLogService adminLogService = new AdminLogService();

    @FXML
    private void initialize() {
        if (currentUserLabel != null) {
            User currentUser = Session.getCurrentUser();
            currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
        }
        configureTable();
        refreshLogs();
    }

    @FXML
    private void handleRefresh() {
        refreshLogs();
    }

    @FXML
    private void handleBack() {
        SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
    }

    private void configureTable() {
        if (adminColumn != null) {
            adminColumn.setCellValueFactory(new PropertyValueFactory<AdminLog, String>("adminUsername"));
        }
        if (actionColumn != null) {
            actionColumn.setCellValueFactory(new PropertyValueFactory<AdminLog, String>("action"));
        }
        if (targetTypeColumn != null) {
            targetTypeColumn.setCellValueFactory(new PropertyValueFactory<AdminLog, String>("targetType"));
        }
        if (targetIdColumn != null) {
            targetIdColumn.setCellValueFactory(new PropertyValueFactory<AdminLog, Long>("targetId"));
        }
        if (detailColumn != null) {
            detailColumn.setCellValueFactory(new PropertyValueFactory<AdminLog, String>("detail"));
        }
        if (createdAtColumn != null) {
            createdAtColumn.setCellValueFactory(new PropertyValueFactory<AdminLog, String>("createdAt"));
        }
    }

    private void refreshLogs() {
        List<AdminLog> logs = adminLogService.listRecent();
        if (logTable != null) {
            logTable.setItems(FXCollections.observableArrayList(logs));
        }
    }
}
