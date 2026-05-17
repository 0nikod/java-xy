package com.campus.secondhand.util;

import com.campus.secondhand.model.Goods;

public final class ViewState {

    private static volatile Goods selectedGoods;

    private ViewState() {
    }

    public static Goods getSelectedGoods() {
        return selectedGoods;
    }

    public static void setSelectedGoods(Goods goods) {
        selectedGoods = goods;
    }

    public static void clearSelectedGoods() {
        selectedGoods = null;
    }
}
