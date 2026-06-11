package com.campus.secondhand.service;

import com.campus.secondhand.dao.AdminLogDao;
import com.campus.secondhand.model.AdminLog;
import com.campus.secondhand.model.User;
import com.campus.secondhand.util.DBUtil;
import com.campus.secondhand.util.DateUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 管理员日志服务，负责记录和查询后台管理操作。
 */
public class AdminLogService {

	private final AdminLogDao adminLogDao;

	/**
	 * 创建管理员日志服务。
	 */
	public AdminLogService() {
		this.adminLogDao = new AdminLogDao();
	}

	/**
	 * 记录一条管理员操作日志。
	 *
	 * @param connection
	 *            外部事务连接
	 * @param admin
	 *            执行操作的管理员
	 * @param action
	 *            操作标识
	 * @param targetType
	 *            目标类型
	 * @param targetId
	 *            目标主键
	 * @param detail
	 *            操作详情
	 * @throws SQLException
	 *             当写入日志失败时抛出
	 */
	public void record(Connection connection, User admin, String action, String targetType, Long targetId,
			String detail) throws SQLException {
		AdminLog log = new AdminLog();
		log.setAdminId(admin.getId());
		log.setAction(action);
		log.setTargetType(targetType);
		log.setTargetId(targetId);
		log.setDetail(detail);
		log.setCreatedAt(DateUtil.nowText());
		adminLogDao.insert(connection, log);
	}

	/**
	 * 使用独立连接记录一条管理员操作日志。
	 *
	 * @param admin
	 *            执行操作的管理员
	 * @param action
	 *            操作标识
	 * @param targetType
	 *            目标类型
	 * @param targetId
	 *            目标主键
	 * @param detail
	 *            操作详情
	 */
	public void record(User admin, String action, String targetType, Long targetId, String detail) {
		// AI 分析这类非事务性操作也需要审计，因此提供独立连接写日志的入口。
		try (Connection connection = DBUtil.getConnection()) {
			record(connection, admin, action, targetType, targetId, detail);
		} catch (SQLException e) {
			throw new IllegalStateException("记录管理员操作日志失败", e);
		}
	}

	/**
	 * 查询最近的管理员操作日志。
	 *
	 * @return 按时间倒序排列的日志列表
	 */
	public List<AdminLog> listRecent() {
		return adminLogDao.listRecent();
	}
}
