package com.campus.secondhand.model;

public class Goods {

	private Long id;
	private Long sellerId;
	private String sellerUsername;
	private String sellerStatus;
	private String title;
	private String category;
	private Double originalPrice;
	private Double currentPrice;
	private Integer conditionLevel;
	private String description;
	private GoodsStatus status;
	private String createdAt;
	private String updatedAt;
	private String primaryImagePath;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSellerId() {
		return sellerId;
	}

	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}

	public String getSellerUsername() {
		return sellerUsername;
	}

	public void setSellerUsername(String sellerUsername) {
		this.sellerUsername = sellerUsername;
	}

	public String getSellerStatus() {
		return sellerStatus;
	}

	public void setSellerStatus(String sellerStatus) {
		this.sellerStatus = sellerStatus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Double getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(Double originalPrice) {
		this.originalPrice = originalPrice;
	}

	public Double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(Double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public Integer getConditionLevel() {
		return conditionLevel;
	}

	public void setConditionLevel(Integer conditionLevel) {
		this.conditionLevel = conditionLevel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GoodsStatus getStatus() {
		return status;
	}

	public void setStatus(GoodsStatus status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getPrimaryImagePath() {
		return primaryImagePath;
	}

	public void setPrimaryImagePath(String primaryImagePath) {
		this.primaryImagePath = primaryImagePath;
	}

	public String getStatusText() {
		if (status == null) {
			return "";
		}
		switch (status) {
			case PENDING: return "待审核";
			case ON_SALE: return "在售";
			case SOLD: return "已售出";
			case OFF_SHELF: return "已下架";
			default: return status.name();
		}
	}

	public String getConditionText() {
		if (conditionLevel == null || conditionLevel < 1) {
			return "";
		}
		int stars = Math.min(conditionLevel, 5);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			sb.append(i < stars ? "★" : "☆");
		}
		return sb.toString();
	}

	public String getPrimaryImageName() {
		if (primaryImagePath == null || primaryImagePath.trim().isEmpty()) {
			return "无图片";
		}
		int slashIndex = Math.max(primaryImagePath.lastIndexOf('/'), primaryImagePath.lastIndexOf('\\'));
		return slashIndex >= 0 ? primaryImagePath.substring(slashIndex + 1) : primaryImagePath;
	}
}
