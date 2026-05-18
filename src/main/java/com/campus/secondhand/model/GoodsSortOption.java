package com.campus.secondhand.model;

public enum GoodsSortOption {
	LATEST("发布时间最新", "g.created_at DESC, g.id DESC"), PRICE_ASC("价格升序", "g.current_price ASC, g.id ASC"), PRICE_DESC(
			"价格降序", "g.current_price DESC, g.id DESC"), CONDITION_DESC("成色优先", "g.condition_level DESC, g.id DESC");

	private final String displayName;
	private final String orderSql;

	GoodsSortOption(String displayName, String orderSql) {
		this.displayName = displayName;
		this.orderSql = orderSql;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getOrderSql() {
		return orderSql;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
