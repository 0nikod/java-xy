package com.campus.secondhand.dao;

import com.campus.secondhand.model.Review;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

	public int insert(Review review) {
		String sql = "INSERT INTO reviews (order_id, goods_id, reviewer_id, seller_id, rating, content, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, review.getOrderId());
			ps.setLong(2, review.getGoodsId());
			ps.setLong(3, review.getReviewerId());
			ps.setLong(4, review.getSellerId());
			ps.setInt(5, review.getRating());
			ps.setString(6, review.getContent());
			ps.setString(7, review.getCreatedAt());
			int affected = ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					review.setId(rs.getLong(1));
				}
			}
			return affected;
		} catch (SQLException e) {
			throw new IllegalStateException("保存评价失败", e);
		}
	}

	public Review findByOrderId(Long orderId) {
		String sql = baseSelectSql() + " WHERE r.order_id = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, orderId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? mapReview(rs) : null;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询评价失败", e);
		}
	}

	public List<Review> listByGoodsId(Long goodsId) {
		return queryReviews(baseSelectSql() + " WHERE r.goods_id = ? ORDER BY r.created_at DESC, r.id DESC", goodsId);
	}

	public List<Review> listByReviewerId(Long reviewerId) {
		return queryReviews(baseSelectSql() + " WHERE r.reviewer_id = ? ORDER BY r.created_at DESC, r.id DESC",
				reviewerId);
	}

	public List<Review> listBySellerId(Long sellerId) {
		return queryReviews(baseSelectSql() + " WHERE r.seller_id = ? ORDER BY r.created_at DESC, r.id DESC", sellerId);
	}

	public List<Review> listAll() {
		return queryReviews(baseSelectSql() + " ORDER BY r.created_at DESC, r.id DESC");
	}

	private List<Review> queryReviews(String sql, Long id) {
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				List<Review> reviews = new ArrayList<Review>();
				while (rs.next()) {
					reviews.add(mapReview(rs));
				}
				return reviews;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询评价失败", e);
		}
	}

	private List<Review> queryReviews(String sql) {
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			List<Review> reviews = new ArrayList<Review>();
			while (rs.next()) {
				reviews.add(mapReview(rs));
			}
			return reviews;
		} catch (SQLException e) {
			throw new IllegalStateException("查询评价失败", e);
		}
	}

	private String baseSelectSql() {
		return "SELECT r.*, o.order_no AS order_no, g.title AS goods_title, ru.username AS reviewer_username, su.username AS seller_username "
				+ "FROM reviews r JOIN orders o ON r.order_id = o.id JOIN goods g ON r.goods_id = g.id "
				+ "JOIN users ru ON r.reviewer_id = ru.id JOIN users su ON r.seller_id = su.id";
	}

	private Review mapReview(ResultSet rs) throws SQLException {
		Review review = new Review();
		review.setId(rs.getLong("id"));
		review.setOrderId(rs.getLong("order_id"));
		review.setOrderNo(rs.getString("order_no"));
		review.setGoodsId(rs.getLong("goods_id"));
		review.setGoodsTitle(rs.getString("goods_title"));
		review.setReviewerId(rs.getLong("reviewer_id"));
		review.setReviewerUsername(rs.getString("reviewer_username"));
		review.setSellerId(rs.getLong("seller_id"));
		review.setSellerUsername(rs.getString("seller_username"));
		review.setRating(rs.getInt("rating"));
		review.setContent(rs.getString("content"));
		review.setCreatedAt(rs.getString("created_at"));
		return review;
	}
}
