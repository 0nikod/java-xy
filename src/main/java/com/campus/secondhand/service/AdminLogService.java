package com.campus.secondhand.service;

import com.campus.secondhand.dao.AdminLogDao;
import com.campus.secondhand.model.AdminLog;
import com.campus.secondhand.model.User;
import com.campus.secondhand.util.DateUtil;
import java.sql.Connection;
import java.sql.SQLException;

public class AdminLogService {

    private final AdminLogDao adminLogDao;

    public AdminLogService() {
        this.adminLogDao = new AdminLogDao();
    }

    public void record(Connection connection, User admin, String action, String targetType, Long targetId, String detail) throws SQLException {
        AdminLog log = new AdminLog();
        log.setAdminId(admin.getId());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setCreatedAt(DateUtil.nowText());
        adminLogDao.insert(connection, log);
    }
}
