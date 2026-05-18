package com.campus.secondhand.dao;

import com.campus.secondhand.model.ChartPoint;
import com.campus.secondhand.model.StatsSummary;
import com.campus.secondhand.model.UserOverview;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计数据访问对象，负责聚合平台运营指标和图表数据。
 */
public class StatisticsDao {

	/**
	 * 加载平台运营汇总数据。
	 */
	public StatsSummary loadSummary() {
		StatsSummary summary = new StatsSummary();
		summary.setTotalUsers(queryInt("SELECT COUNT(*) FROM users"));
		summary.setNormalUsers(queryInt("SELECT COUNT(*) FROM users WHERE status = 'NORMAL'"));
		summary.setBannedUsers(queryInt("SELECT COUNT(*) FROM users WHERE status = 'BANNED'"));
		summary.setTotalGoods(queryInt("SELECT COUNT(*) FROM goods"));
		summary.setPendingGoods(queryInt("SELECT COUNT(*) FROM goods WHERE status = 'PENDING'"));
		summary.setOnSaleGoods(queryInt("SELECT COUNT(*) FROM goods WHERE status = 'ON_SALE'"));
		summary.setSoldGoods(queryInt("SELECT COUNT(*) FROM goods WHERE status = 'SOLD'"));
		summary.setOffShelfGoods(queryInt("SELECT COUNT(*) FROM goods WHERE status = 'OFF_SHELF'"));
		summary.setTotalOrders(queryInt("SELECT COUNT(*) FROM orders"));
		summary.setTotalRevenue(queryDouble("SELECT COALESCE(SUM(deal_price), 0) FROM orders"));
		return summary;
	}

	/**
	 * 加载用户概览列表，可按用户名关键字过滤。
	 */
	public List<UserOverview> listUserOverviews(String keyword) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT u.id, u.username, u.phone, u.role, u.status, u.created_at, ")
				.append("COALESCE(g.goods_count, 0) AS goods_count, ")
				.append("COALESCE(o.sold_count, 0) AS sold_count ").append("FROM users u ")
				.append("LEFT JOIN (SELECT seller_id, COUNT(*) AS goods_count FROM goods GROUP BY seller_id) g ON g.seller_id = u.id ")
				.append("LEFT JOIN (SELECT seller_id, COUNT(*) AS sold_count FROM orders GROUP BY seller_id) o ON o.seller_id = u.id ");

		List<Object> params = new ArrayList<Object>();
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append("WHERE u.username LIKE ? ");
			params.add("%" + keyword.trim() + "%");
		}
		sql.append("ORDER BY u.created_at DESC, u.id DESC");

		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql.toString())) {
			bindParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				List<UserOverview> overviews = new ArrayList<UserOverview>();
				while (rs.next()) {
					UserOverview overview = new UserOverview();
					overview.setId(rs.getLong("id"));
					overview.setUsername(rs.getString("username"));
					overview.setPhone(rs.getString("phone"));
					overview.setRole(rs.getString("role"));
					overview.setStatus(rs.getString("status"));
					overview.setCreatedAt(rs.getString("created_at"));
					overview.setPublishedGoodsCount(rs.getInt("goods_count"));
					overview.setSoldOrderCount(rs.getInt("sold_count"));
					overviews.add(overview);
				}
				return overviews;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询用户统计失败", e);
		}
	}

	/**
	 * 统计商品分类分布。
	 */
	public List<ChartPoint> listGoodsCategoryStats() {
		return queryChartPoints(
				"SELECT category AS label, COUNT(*) AS value FROM goods GROUP BY category ORDER BY COUNT(*) DESC, category ASC");
	}

	/**
	 * 统计商品状态分布。
	 */
	public List<ChartPoint> listGoodsStatusStats() {
		return queryChartPoints(
				"SELECT status AS label, COUNT(*) AS value FROM goods GROUP BY status ORDER BY COUNT(*) DESC, status ASC");
	}

	/**
	 * 统计最近的订单趋势数据，按日期升序返回。
	 */
	public List<ChartPoint> listRecentOrderTrend() {
		String sql = "SELECT substr(created_at, 1, 10) AS label, COUNT(*) AS value "
				+ "FROM orders GROUP BY substr(created_at, 1, 10) ORDER BY label DESC LIMIT 7";
		List<ChartPoint> points = queryChartPoints(sql);
		List<ChartPoint> ordered = new ArrayList<ChartPoint>();
		for (int i = points.size() - 1; i >= 0; i--) {
			ordered.add(points.get(i));
		}
		return ordered;
	}

	/**
	 * 执行通用图表聚合查询。
	 */
	private List<ChartPoint> queryChartPoints(String sql) {
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			List<ChartPoint> points = new ArrayList<ChartPoint>();
			while (rs.next()) {
				points.add(new ChartPoint(rs.getString("label"), rs.getDouble("value")));
			}
			return points;
		} catch (SQLException e) {
			throw new IllegalStateException("查询统计图表数据失败", e);
		}
	}

	/**
	 * 执行返回单个整数结果的聚合查询。
	 */
	private int queryInt(String sql) {
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		} catch (SQLException e) {
			throw new IllegalStateException("查询统计数据失败", e);
		}
	}

	/**
	 * 执行返回单个浮点结果的聚合查询。
	 */
	private double queryDouble(String sql) {
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			return rs.next() ? rs.getDouble(1) : 0.0;
		} catch (SQLException e) {
			throw new IllegalStateException("查询统计数据失败", e);
		}
	}

	/**
	 * 为预编译语句绑定查询参数。
	 */
	private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
	}
}
