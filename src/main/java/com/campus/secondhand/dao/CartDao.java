package com.campus.secondhand.dao;

import com.campus.secondhand.model.CartItem;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartDao {

	public int insert(CartItem cartItem) {
		String sql = "INSERT INTO cart_items (user_id, goods_id, created_at) VALUES (?, ?, ?)";
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, cartItem.getUserId());
			ps.setLong(2, cartItem.getGoodsId());
			ps.setString(3, cartItem.getCreatedAt());
			int affected = ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					cartItem.setId(rs.getLong(1));
				}
			}
			return affected;
		} catch (SQLException e) {
			throw new IllegalStateException("加入购物车失败", e);
		}
	}

	public CartItem findByUserAndGoods(Long userId, Long goodsId) {
		String sql = baseSelectSql() + " WHERE c.user_id = ? AND c.goods_id = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setLong(2, goodsId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapCartItem(rs) : null;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询购物车失败", e);
		}
	}

	public CartItem findById(Long cartItemId) {
		String sql = baseSelectSql() + " WHERE c.id = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, cartItemId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapCartItem(rs) : null;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询购物车失败", e);
		}
	}

	public List<CartItem> listByUserId(Long userId) {
		String sql = baseSelectSql() + " WHERE c.user_id = ? ORDER BY c.created_at DESC, c.id DESC";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				List<CartItem> items = new ArrayList<CartItem>();
				while (rs.next()) {
					items.add(mapCartItem(rs));
				}
				return items;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询购物车失败", e);
		}
	}

	public int deleteByIdAndUser(Long cartItemId, Long userId) {
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection
						.prepareStatement("DELETE FROM cart_items WHERE id = ? AND user_id = ?")) {
			ps.setLong(1, cartItemId);
			ps.setLong(2, userId);
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("移除购物车商品失败", e);
		}
	}

	public void deleteByIds(Connection connection, Long userId, List<Long> cartItemIds) throws SQLException {
		String sql = "DELETE FROM cart_items WHERE id = ? AND user_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			for (Long cartItemId : cartItemIds) {
				ps.setLong(1, cartItemId);
				ps.setLong(2, userId);
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	public void deleteByGoodsId(Connection connection, Long goodsId) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement("DELETE FROM cart_items WHERE goods_id = ?")) {
			ps.setLong(1, goodsId);
			ps.executeUpdate();
		}
	}

	private String baseSelectSql() {
		return "SELECT c.id AS cart_id, c.user_id AS cart_user_id, c.goods_id AS cart_goods_id, c.created_at AS cart_created_at, "
				+ "g.*, u.username AS seller_username, u.status AS seller_status, gi.image_path AS primary_image_path "
				+ "FROM cart_items c JOIN goods g ON c.goods_id = g.id JOIN users u ON g.seller_id = u.id "
				+ "LEFT JOIN goods_images gi ON gi.goods_id = g.id AND gi.is_primary = 1";
	}

	private CartItem mapCartItem(ResultSet rs) throws SQLException {
		CartItem item = new CartItem();
		item.setId(rs.getLong("cart_id"));
		item.setUserId(rs.getLong("cart_user_id"));
		item.setGoodsId(rs.getLong("cart_goods_id"));
		item.setCreatedAt(rs.getString("cart_created_at"));
		Goods goods = new Goods();
		goods.setId(rs.getLong("id"));
		goods.setSellerId(rs.getLong("seller_id"));
		goods.setSellerUsername(rs.getString("seller_username"));
		goods.setSellerStatus(rs.getString("seller_status"));
		goods.setTitle(rs.getString("title"));
		goods.setCategory(rs.getString("category"));
		goods.setOriginalPrice(rs.getDouble("original_price"));
		goods.setCurrentPrice(rs.getDouble("current_price"));
		goods.setConditionLevel(rs.getInt("condition_level"));
		goods.setDescription(rs.getString("description"));
		goods.setStatus(GoodsStatus.valueOf(rs.getString("status")));
		goods.setCreatedAt(rs.getString("created_at"));
		goods.setUpdatedAt(rs.getString("updated_at"));
		goods.setPrimaryImagePath(rs.getString("primary_image_path"));
		item.setGoods(goods);
		return item;
	}
}
