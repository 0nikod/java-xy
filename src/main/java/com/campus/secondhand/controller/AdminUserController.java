package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserOverview;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.StatisticsService;
import com.campus.secondhand.service.UserService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.Session;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminUserController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private TextField keywordField;

    @FXML
    private TableView<UserOverview> userTable;

    @FXML
    private TableColumn<UserOverview, String> usernameColumn;

    @FXML
    private TableColumn<UserOverview, String> phoneColumn;

    @FXML
    private TableColumn<UserOverview, String> roleColumn;

    @FXML
    private TableColumn<UserOverview, String> statusColumn;

    @FXML
    private TableColumn<UserOverview, Integer> goodsCountColumn;

    @FXML
    private TableColumn<UserOverview, Integer> soldCountColumn;

    private final StatisticsService statisticsService = new StatisticsService();
    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        configureTable();
        User currentUser = Session.getCurrentUser();
        if (currentUserLabel != null) {
            currentUserLabel.setText(currentUser == null ? "未登录" : "当前管理员：" + currentUser.getUsername());
        }
        refreshUsers();
    }

    @FXML
    private void handleSearch() {
        refreshUsers();
    }

    @FXML
    private void handleReset() {
        if (keywordField != null) {
            keywordField.clear();
        }
        refreshUsers();
    }

    @FXML
    private void handleBan() {
        changeStatus(true);
    }

    @FXML
    private void handleUnban() {
        changeStatus(false);
    }

    @FXML
    private void handleBack() {
        SceneManager.show("admin_home.fxml", AppConfig.getAppTitle());
    }

    private void configureTable() {
        if (usernameColumn != null) {
            usernameColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, String>("username"));
        }
        if (phoneColumn != null) {
            phoneColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, String>("phone"));
        }
        if (roleColumn != null) {
            roleColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, String>("role"));
        }
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, String>("status"));
        }
        if (goodsCountColumn != null) {
            goodsCountColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, Integer>("publishedGoodsCount"));
        }
        if (soldCountColumn != null) {
            soldCountColumn.setCellValueFactory(new PropertyValueFactory<UserOverview, Integer>("soldOrderCount"));
        }
    }

    private void refreshUsers() {
        String keyword = keywordField == null ? null : keywordField.getText();
        List<UserOverview> overviews = statisticsService.listUserOverviews(keyword);
        if (userTable != null) {
            userTable.setItems(FXCollections.observableArrayList(overviews));
        }
        if (summaryLabel != null) {
            summaryLabel.setText("已加载 " + overviews.size() + " 个用户。");
        }
    }

    private void changeStatus(boolean ban) {
        UserOverview selected = userTable == null ? null : userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("提示", "请先选择一个用户。");
            return;
        }
        try {
            User admin = Session.getCurrentUser();
            if (ban) {
                userService.banUser(admin, selected.getId());
                AlertUtil.showInfo("操作成功", "用户已封禁。");
            } else {
                userService.unbanUser(admin, selected.getId());
                AlertUtil.showInfo("操作成功", "用户已解封。");
            }
            refreshUsers();
        } catch (BusinessException ex) {
            AlertUtil.showWarning("操作失败", ex.getMessage());
        }
    }
}
