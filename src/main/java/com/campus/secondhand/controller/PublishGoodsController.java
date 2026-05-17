package com.campus.secondhand.controller;

import com.campus.secondhand.app.SceneManager;
import com.campus.secondhand.config.AppConfig;
import com.campus.secondhand.model.GoodsCategory;
import com.campus.secondhand.model.User;
import com.campus.secondhand.service.BusinessException;
import com.campus.secondhand.service.GoodsService;
import com.campus.secondhand.util.AlertUtil;
import com.campus.secondhand.util.Session;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PublishGoodsController {

    @FXML
    private Label currentUserLabel;

    @FXML
    private TextField titleField;

    @FXML
    private ChoiceBox<String> categoryBox;

    @FXML
    private TextField originalPriceField;

    @FXML
    private TextField currentPriceField;

    @FXML
    private TextField conditionField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextArea aiSuggestionArea;

    private final GoodsService goodsService = new GoodsService();

    @FXML
    private void initialize() {
        if (categoryBox != null) {
            categoryBox.setItems(FXCollections.observableArrayList(categories()));
            categoryBox.getSelectionModel().selectFirst();
        }
        if (conditionField != null) {
            conditionField.setText("8");
        }
        User currentUser = Session.getCurrentUser();
        if (currentUserLabel != null) {
            currentUserLabel.setText(currentUser == null ? "未登录" : "当前用户：" + currentUser.getUsername());
        }
    }

    @FXML
    private void handleOptimize() {
        if (descriptionArea == null) {
            return;
        }
        String suggestion = goodsService.optimizeDescription(descriptionArea.getText());
        if (aiSuggestionArea != null) {
            aiSuggestionArea.setText(suggestion);
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                SceneManager.show("login.fxml", AppConfig.getAppTitle());
                return;
            }
            goodsService.publishGoods(
                    currentUser,
                    getText(titleField),
                    getText(categoryBox),
                    parseDouble(getText(originalPriceField), "原价不能为空"),
                    parseDouble(getText(currentPriceField), "现价不能为空"),
                    parseInt(getText(conditionField), "新旧程度不能为空"),
                    getText(descriptionArea));
            AlertUtil.showInfo("提交成功", "商品已进入待审核状态。");
            SceneManager.show("home.fxml", AppConfig.getAppTitle());
        } catch (BusinessException ex) {
            AlertUtil.showWarning("提交失败", ex.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.show("home.fxml", AppConfig.getAppTitle());
    }

    private List<String> categories() {
        return FXCollections.observableArrayList(
                GoodsCategory.TEXTBOOK.getDisplayName(),
                GoodsCategory.DIGITAL.getDisplayName(),
                GoodsCategory.CLOTHING.getDisplayName(),
                GoodsCategory.DAILY.getDisplayName(),
                GoodsCategory.OTHER.getDisplayName());
    }

    private String getText(TextField field) {
        return field == null ? null : field.getText();
    }

    private String getText(TextArea field) {
        return field == null ? null : field.getText();
    }

    private String getText(ChoiceBox<String> field) {
        return field == null ? null : field.getSelectionModel().getSelectedItem();
    }

    private double parseDouble(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(message);
        }
    }

    private int parseInt(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(message);
        }
    }
}
