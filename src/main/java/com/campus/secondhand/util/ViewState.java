package com.campus.secondhand.util;

import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.Order;

public final class ViewState {

	private static volatile Goods selectedGoods;
	private static volatile Order selectedOrder;

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

	public static Order getSelectedOrder() {
		return selectedOrder;
	}

	public static void setSelectedOrder(Order order) {
		selectedOrder = order;
	}

	public static void clearSelectedOrder() {
		selectedOrder = null;
	}
}
