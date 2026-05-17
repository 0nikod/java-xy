package com.campus.secondhand.dao;

import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserRole;
import com.campus.secondhand.model.UserStatus;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询用户失败", e);
        }
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询用户失败", e);
        }
    }

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

    private User mapUser(ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return null;
        }
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
