package com.campus.secondhand.model;

/**
 * 平台统计汇总实体，用于后台看板展示。
 */
public class StatsSummary {

	private int totalUsers;
	private int normalUsers;
	private int bannedUsers;
	private int totalGoods;
	private int pendingGoods;
	private int onSaleGoods;
	private int soldGoods;
	private int offShelfGoods;
	private int totalOrders;
	private int todayOrders;
	private double totalRevenue;

	public int getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}

	public int getNormalUsers() {
		return normalUsers;
	}

	public void setNormalUsers(int normalUsers) {
		this.normalUsers = normalUsers;
	}

	public int getBannedUsers() {
		return bannedUsers;
	}

	public void setBannedUsers(int bannedUsers) {
		this.bannedUsers = bannedUsers;
	}

	public int getTotalGoods() {
		return totalGoods;
	}

	public void setTotalGoods(int totalGoods) {
		this.totalGoods = totalGoods;
	}

	public int getPendingGoods() {
		return pendingGoods;
	}

	public void setPendingGoods(int pendingGoods) {
		this.pendingGoods = pendingGoods;
	}

	public int getOnSaleGoods() {
		return onSaleGoods;
	}

	public void setOnSaleGoods(int onSaleGoods) {
		this.onSaleGoods = onSaleGoods;
	}

	public int getSoldGoods() {
		return soldGoods;
	}

	public void setSoldGoods(int soldGoods) {
		this.soldGoods = soldGoods;
	}

	public int getOffShelfGoods() {
		return offShelfGoods;
	}

	public void setOffShelfGoods(int offShelfGoods) {
		this.offShelfGoods = offShelfGoods;
	}

	public int getTotalOrders() {
		return totalOrders;
	}

	public void setTotalOrders(int totalOrders) {
		this.totalOrders = totalOrders;
	}

	public int getTodayOrders() {
		return todayOrders;
	}

	public void setTodayOrders(int todayOrders) {
		this.todayOrders = todayOrders;
	}

	public double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}
}
