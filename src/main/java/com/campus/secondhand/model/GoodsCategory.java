package com.campus.secondhand.model;

public enum GoodsCategory {
    TEXTBOOK("教材"),
    DIGITAL("数码"),
    CLOTHING("服饰"),
    DAILY("生活用品"),
    OTHER("其他");

    private final String displayName;

    GoodsCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
