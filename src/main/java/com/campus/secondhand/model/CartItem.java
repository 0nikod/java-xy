package com.campus.secondhand.model;

public class CartItem {

	private Long id;
	private Long userId;
	private Long goodsId;
	private String createdAt;
	private Goods goods;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public Goods getGoods() {
		return goods;
	}

	public void setGoods(Goods goods) {
		this.goods = goods;
	}

	public String getGoodsTitle() {
		return goods == null ? "" : goods.getTitle();
	}

	public String getCategory() {
		return goods == null ? "" : goods.getCategory();
	}

	public Double getCurrentPrice() {
		return goods == null ? null : goods.getCurrentPrice();
	}

	public String getSellerUsername() {
		return goods == null ? "" : goods.getSellerUsername();
	}

	public String getGoodsStatusText() {
		return goods == null ? "" : goods.getStatusText();
	}

	public String getPrimaryImageName() {
		return goods == null ? "" : goods.getPrimaryImageName();
	}
}
