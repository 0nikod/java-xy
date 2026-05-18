package com.campus.secondhand.dao;

import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.OrderStatus;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

	public int insert(Connection connection, Order order) throws SQLException {
		String sql = "INSERT INTO orders (order_no, goods_id, buyer_id, seller_id, deal_price, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			bindOrder(ps, order);
			int affected = ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					order.setId(rs.getLong(1));
				}
			}
			return affected;
		}
	}

	public List<Order> listByBuyerId(Long buyerId) {
		String sql = "SELECT o.*, g.title AS goods_title, bu.username AS buyer_username, su.username AS seller_username "
				+ "FROM orders o " + "JOIN goods g ON o.goods_id = g.id " + "JOIN users bu ON o.buyer_id = bu.id "
				+ "JOIN users su ON o.seller_id = su.id "
				+ "WHERE o.buyer_id = ? ORDER BY o.created_at DESC, o.id DESC";
		return queryOrders(sql, buyerId);
	}

	public List<Order> listBySellerId(Long sellerId) {
		String sql = "SELECT o.*, g.title AS goods_title, bu.username AS buyer_username, su.username AS seller_username "
				+ "FROM orders o " + "JOIN goods g ON o.goods_id = g.id " + "JOIN users bu ON o.buyer_id = bu.id "
				+ "JOIN users su ON o.seller_id = su.id "
				+ "WHERE o.seller_id = ? ORDER BY o.created_at DESC, o.id DESC";
		return queryOrders(sql, sellerId);
	}

	public Order findByOrderNo(String orderNo) {
		String sql = "SELECT o.*, g.title AS goods_title, bu.username AS buyer_username, su.username AS seller_username "
				+ "FROM orders o " + "JOIN goods g ON o.goods_id = g.id " + "JOIN users bu ON o.buyer_id = bu.id "
				+ "JOIN users su ON o.seller_id = su.id " + "WHERE o.order_no = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, orderNo);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return mapOrder(rs);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询订单失败", e);
		}
	}

	private List<Order> queryOrders(String sql, Long userId) {
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				List<Order> orders = new ArrayList<Order>();
				while (rs.next()) {
					orders.add(mapOrder(rs));
				}
				return orders;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询订单失败", e);
		}
	}

	private void bindOrder(PreparedStatement ps, Order order) throws SQLException {
		ps.setString(1, order.getOrderNo());
		ps.setLong(2, order.getGoodsId());
		ps.setLong(3, order.getBuyerId());
		ps.setLong(4, order.getSellerId());
		ps.setDouble(5, order.getDealPrice());
		ps.setString(6, order.getStatus().name());
		ps.setString(7, order.getCreatedAt());
	}

	private Order mapOrder(ResultSet rs) throws SQLException {
		Order order = new Order();
		order.setId(rs.getLong("id"));
		order.setOrderNo(rs.getString("order_no"));
		order.setGoodsId(rs.getLong("goods_id"));
		order.setGoodsTitle(rs.getString("goods_title"));
		order.setBuyerId(rs.getLong("buyer_id"));
		order.setBuyerUsername(rs.getString("buyer_username"));
		order.setSellerId(rs.getLong("seller_id"));
		order.setSellerUsername(rs.getString("seller_username"));
		order.setDealPrice(rs.getDouble("deal_price"));
		order.setStatus(OrderStatus.valueOf(rs.getString("status")));
		order.setCreatedAt(rs.getString("created_at"));
		return order;
	}
}
