package com.campus.secondhand.dao;

import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsSortOption;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoodsDao {

	public long insert(Goods goods) {
		// 新增商品基础信息；图片信息由 goods_images 单独维护，避免主表过重。
		String sql = "INSERT INTO goods (seller_id, title, category, original_price, current_price, condition_level, description, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			bindGoodsInsert(ps, goods);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					goods.setId(id);
					return id;
				}
			}
			throw new IllegalStateException("插入商品失败，未获取到主键");
		} catch (SQLException e) {
			throw new IllegalStateException("插入商品失败", e);
		}
	}

	public Goods findById(Long id) {
		// 详情查询：联表带出卖家名称和主图路径，方便详情页/审核页直接展示。
		String sql = "SELECT g.*, u.username AS seller_username, u.status AS seller_status, gi.image_path AS primary_image_path "
				+ "FROM goods g JOIN users u ON g.seller_id = u.id "
				+ "LEFT JOIN goods_images gi ON gi.goods_id = g.id AND gi.is_primary = 1 WHERE g.id = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return mapGoods(rs);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询商品失败", e);
		}
	}

	public List<Goods> listPendingGoods() {
		// 后台审核页只看待审核商品，按发布时间倒序让最新内容优先处理。
		String sql = "SELECT g.*, u.username AS seller_username, u.status AS seller_status, gi.image_path AS primary_image_path "
				+ "FROM goods g JOIN users u ON g.seller_id = u.id "
				+ "LEFT JOIN goods_images gi ON gi.goods_id = g.id AND gi.is_primary = 1 "
				+ "WHERE g.status = 'PENDING' ORDER BY g.created_at DESC, g.id DESC";
		return queryGoodsList(sql, null);
	}

	public List<Goods> listUserPublishedGoods(Long sellerId) {
		// 个人中心的“我的发布”列表，直接按卖家筛选。
		String sql = "SELECT g.*, u.username AS seller_username, u.status AS seller_status, gi.image_path AS primary_image_path "
				+ "FROM goods g JOIN users u ON g.seller_id = u.id "
				+ "LEFT JOIN goods_images gi ON gi.goods_id = g.id AND gi.is_primary = 1 "
				+ "WHERE g.seller_id = ? ORDER BY g.created_at DESC, g.id DESC";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, sellerId);
			try (ResultSet rs = ps.executeQuery()) {
				return mapGoodsList(rs);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询用户商品失败", e);
		}
	}

	public List<Goods> listPublicGoods(String keyword, String category, GoodsSortOption sortOption) {
		// 首页商品列表只展示可见商品，同时排除被封禁卖家发布的内容。
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT g.*, u.username AS seller_username, u.status AS seller_status ")
				.append(", gi.image_path AS primary_image_path ")
				.append("FROM goods g JOIN users u ON g.seller_id = u.id ")
				.append("LEFT JOIN goods_images gi ON gi.goods_id = g.id AND gi.is_primary = 1 ")
				.append("WHERE g.status = 'ON_SALE' AND u.status = 'NORMAL'");

		List<Object> params = new ArrayList<Object>();
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (g.title LIKE ? OR g.category LIKE ? OR g.description LIKE ? OR u.username LIKE ?)");
			String like = "%" + keyword.trim() + "%";
			params.add(like);
			params.add(like);
			params.add(like);
			params.add(like);
		}
		if (category != null && !category.trim().isEmpty()) {
			sql.append(" AND g.category = ?");
			params.add(category.trim());
		}
		sql.append(" ORDER BY ");
		sql.append(sortOption == null ? GoodsSortOption.LATEST.getOrderSql() : sortOption.getOrderSql());
		return queryGoodsList(sql.toString(), params);
	}

	public int updateStatus(Long goodsId, GoodsStatus status) {
		// 更新商品状态并同步更新时间，供审核、下架等业务复用。
		String sql = "UPDATE goods SET status = ?, updated_at = ? WHERE id = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setString(2, com.campus.secondhand.util.DateUtil.nowText());
			ps.setLong(3, goodsId);
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("更新商品状态失败", e);
		}
	}

	public int markSold(Connection connection, Long goodsId) throws SQLException {
		// 订单成交后将商品直接标记为已售，要求与订单写入在同一事务里完成。
		String sql = "UPDATE goods SET status = 'SOLD', updated_at = ? WHERE id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, com.campus.secondhand.util.DateUtil.nowText());
			ps.setLong(2, goodsId);
			return ps.executeUpdate();
		}
	}

	public Goods loadById(Connection connection, Long id) throws SQLException {
		// 事务内读取商品详情，避免购买流程中反复打开新连接。
		String sql = "SELECT g.*, u.username AS seller_username, u.status AS seller_status, gi.image_path AS primary_image_path "
				+ "FROM goods g JOIN users u ON g.seller_id = u.id "
				+ "LEFT JOIN goods_images gi ON gi.goods_id = g.id AND gi.is_primary = 1 WHERE g.id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return mapGoods(rs);
			}
		}
	}

	public int insert(Connection connection, Goods goods) throws SQLException {
		// 事务内新增商品，用于需要和其它表配合处理的业务场景。
		String sql = "INSERT INTO goods (seller_id, title, category, original_price, current_price, condition_level, description, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			bindGoodsInsert(ps, goods);
			int affected = ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					goods.setId(rs.getLong(1));
				}
			}
			return affected;
		}
	}

	public int deleteById(Connection connection, Long goodsId) throws SQLException {
		// 管理员删除违规商品时使用，配合事务统一清理关联记录。
		try (PreparedStatement ps = connection.prepareStatement("DELETE FROM goods WHERE id = ?")) {
			ps.setLong(1, goodsId);
			return ps.executeUpdate();
		}
	}

	private void bindGoodsInsert(PreparedStatement ps, Goods goods) throws SQLException {
		// 商品新增参数绑定，顺序与 insert SQL 保持一致。
		ps.setLong(1, goods.getSellerId());
		ps.setString(2, goods.getTitle());
		ps.setString(3, goods.getCategory());
		ps.setDouble(4, goods.getOriginalPrice());
		ps.setDouble(5, goods.getCurrentPrice());
		ps.setInt(6, goods.getConditionLevel());
		ps.setString(7, goods.getDescription());
		ps.setString(8, goods.getStatus().name());
		ps.setString(9, goods.getCreatedAt());
		ps.setString(10, goods.getUpdatedAt());
	}

	private List<Goods> queryGoodsList(String sql, List<Object> params) {
		// 统一的商品列表查询封装，支持动态参数绑定与结果映射。
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					Object param = params.get(i);
					if (param instanceof String) {
						ps.setString(i + 1, (String) param);
					} else if (param instanceof Long) {
						ps.setLong(i + 1, (Long) param);
					} else if (param instanceof Integer) {
						ps.setInt(i + 1, (Integer) param);
					} else if (param instanceof Double) {
						ps.setDouble(i + 1, (Double) param);
					} else {
						ps.setObject(i + 1, param);
					}
				}
			}
			try (ResultSet rs = ps.executeQuery()) {
				return mapGoodsList(rs);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询商品失败", e);
		}
	}

	private List<Goods> mapGoodsList(ResultSet rs) throws SQLException {
		// 将 ResultSet 的多行结果转换为商品列表。
		List<Goods> goods = new ArrayList<Goods>();
		while (rs.next()) {
			goods.add(mapRow(rs));
		}
		return goods;
	}

	private Goods mapGoods(ResultSet rs) throws SQLException {
		// 单条查询：没有记录时返回 null，方便上层统一判断“商品不存在”。
		if (!rs.next()) {
			return null;
		}
		return mapRow(rs);
	}

	private Goods mapRow(ResultSet rs) throws SQLException {
		// 字段映射统一集中在这里，避免 SQL 变更时在多个位置散落修改。
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
		return goods;
	}
}
