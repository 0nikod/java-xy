package com.campus.secondhand.dao;

import com.campus.secondhand.model.AdminLog;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminLogDao {

    public int insert(Connection connection, AdminLog log) throws SQLException {
        String sql = "INSERT INTO admin_logs (admin_id, action, target_type, target_id, detail, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bindLog(ps, log);
            int affected = ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getLong(1));
                }
            }
            return affected;
        }
    }

    public List<AdminLog> listRecent() {
        String sql = "SELECT al.*, u.username AS admin_username FROM admin_logs al JOIN users u ON al.admin_id = u.id ORDER BY al.created_at DESC, al.id DESC";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<AdminLog> logs = new ArrayList<AdminLog>();
            while (rs.next()) {
                logs.add(mapLog(rs));
            }
            return logs;
        } catch (SQLException e) {
            throw new IllegalStateException("查询管理员日志失败", e);
        }
    }

    private void bindLog(PreparedStatement ps, AdminLog log) throws SQLException {
        ps.setLong(1, log.getAdminId());
        ps.setString(2, log.getAction());
        ps.setString(3, log.getTargetType());
        if (log.getTargetId() == null) {
            ps.setNull(4, java.sql.Types.INTEGER);
        } else {
            ps.setLong(4, log.getTargetId());
        }
        ps.setString(5, log.getDetail());
        ps.setString(6, log.getCreatedAt());
    }

    private AdminLog mapLog(ResultSet rs) throws SQLException {
        AdminLog log = new AdminLog();
        log.setId(rs.getLong("id"));
        log.setAdminId(rs.getLong("admin_id"));
        log.setAdminUsername(rs.getString("admin_username"));
        log.setAction(rs.getString("action"));
        log.setTargetType(rs.getString("target_type"));
        long targetId = rs.getLong("target_id");
        if (rs.wasNull()) {
            log.setTargetId(null);
        } else {
            log.setTargetId(targetId);
        }
        log.setDetail(rs.getString("detail"));
        log.setCreatedAt(rs.getString("created_at"));
        return log;
    }
}
