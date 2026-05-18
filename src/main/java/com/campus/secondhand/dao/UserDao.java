package com.campus.secondhand.dao;

import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserRole;
import com.campus.secondhand.model.UserStatus;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象，封装用户表的查询和写入操作。
 */
public class UserDao {

	/**
	 * 根据用户名查询用户。
	 */
	public User findByUsername(String username) {
		String sql = "SELECT * FROM users WHERE username = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				return mapUser(rs);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询用户失败", e);
		}
	}

	/**
	 * 根据主键查询用户。
	 */
	public User findById(Long id) {
		String sql = "SELECT * FROM users WHERE id = ?";
		try (Connection connection = DBUtil.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				return mapUser(rs);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询用户失败", e);
		}
	}

	/**
	 * 插入一条用户记录并回填主键。
	 */
	public long insert(User user) {
		String sql = "INSERT INTO users (username, password_hash, phone, role, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPasswordHash());
			ps.setString(3, user.getPhone());
			ps.setString(4, user.getRole().name());
			ps.setString(5, user.getStatus().name());
			ps.setString(6, user.getCreatedAt());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					user.setId(id);
					return id;
				}
			}
			throw new IllegalStateException("插入用户失败，未获取到主键");
		} catch (SQLException e) {
			throw new IllegalStateException("插入用户失败", e);
		}
	}

	/**
	 * 查询用户列表，可按用户名关键字过滤。
	 */
	public List<User> listUsers(String keyword) {
		StringBuilder sql = new StringBuilder("SELECT * FROM users");
		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
		if (hasKeyword) {
			sql.append(" WHERE username LIKE ?");
		}
		sql.append(" ORDER BY created_at DESC, id DESC");

		try (Connection connection = DBUtil.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql.toString())) {
			if (hasKeyword) {
				ps.setString(1, "%" + keyword.trim() + "%");
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<User> users = new ArrayList<User>();
				while (rs.next()) {
					users.add(mapCurrentRow(rs));
				}
				return users;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("查询用户列表失败", e);
		}
	}

	/**
	 * 在指定连接中更新用户状态。
	 */
	public int updateStatus(Connection connection, Long userId, UserStatus status) throws SQLException {
		String sql = "UPDATE users SET status = ? WHERE id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setLong(2, userId);
			return ps.executeUpdate();
		}
	}

	/**
	 * 将结果集映射为单个用户；如果当前结果集没有记录则返回 {@code null}。
	 */
	private User mapUser(ResultSet rs) throws SQLException {
		if (!rs.next()) {
			return null;
		}
		return mapCurrentRow(rs);
	}

	/**
	 * 将当前行映射为用户对象。
	 */
	private User mapCurrentRow(ResultSet rs) throws SQLException {
		User user = new User();
		user.setId(rs.getLong("id"));
		user.setUsername(rs.getString("username"));
		user.setPasswordHash(rs.getString("password_hash"));
		user.setPhone(rs.getString("phone"));
		user.setRole(UserRole.valueOf(rs.getString("role")));
		user.setStatus(UserStatus.valueOf(rs.getString("status")));
		user.setCreatedAt(rs.getString("created_at"));
		return user;
	}
}
