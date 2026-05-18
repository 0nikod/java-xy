package com.campus.secondhand.service;

import com.campus.secondhand.dao.UserDao;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserRole;
import com.campus.secondhand.model.UserStatus;
import com.campus.secondhand.util.DateUtil;
import com.campus.secondhand.util.DBUtil;
import com.campus.secondhand.util.PasswordUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 用户领域服务，负责注册、登录以及管理员对用户状态的变更。
 */
public class UserService {

    private final UserDao userDao;
    private final AdminLogService adminLogService;

    /**
     * 创建用户服务并初始化底层依赖。
     */
    public UserService() {
        this.userDao = new UserDao();
        this.adminLogService = new AdminLogService();
    }

    /**
     * 注册普通用户。
     *
     * @param username 用户名
     * @param password 明文密码
     * @param phone 联系方式
     * @return 新建的用户对象
     * @throws BusinessException 当参数为空、用户名已存在或其他业务规则不满足时抛出
     */
    public User register(String username, String password, String phone) {
        String normalizedUsername = normalizeRequired(username, "用户名不能为空");
        String normalizedPassword = normalizeRequired(password, "密码不能为空");
        String normalizedPhone = normalizeRequired(phone, "联系方式不能为空");

        if (userDao.findByUsername(normalizedUsername) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(PasswordUtil.hashPassword(normalizedPassword));
        user.setPhone(normalizedPhone);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.NORMAL);
        user.setCreatedAt(DateUtil.nowText());
        userDao.insert(user);
        return user;
    }

    /**
     * 校验用户名和密码并返回登录用户。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 匹配的用户对象
     * @throws BusinessException 当账号不存在、被封禁或密码错误时抛出
     */
    public User login(String username, String password) {
        String normalizedUsername = normalizeRequired(username, "用户名不能为空");
        String normalizedPassword = normalizeRequired(password, "密码不能为空");

        User user = userDao.findByUsername(normalizedUsername);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("账号已被封禁");
        }
        if (!PasswordUtil.matches(normalizedPassword, user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        return user;
    }

    /**
     * 根据主键查询用户。
     *
     * @param id 用户主键
     * @return 用户对象，未找到时返回 {@code null}
     */
    public User getById(Long id) {
        return userDao.findById(id);
    }

    /**
     * 查询用户列表。
     *
     * @param keyword 用户名关键字，可为空
     * @return 用户列表
     */
    public List<User> listUsers(String keyword) {
        return userDao.listUsers(keyword);
    }

    /**
     * 封禁指定用户。
     *
     * @param admin 执行操作的管理员
     * @param userId 目标用户主键
     * @return 状态变更后的用户对象
     */
    public User banUser(User admin, Long userId) {
        return changeUserStatus(admin, userId, UserStatus.BANNED, "BAN_USER", "封禁用户");
    }

    /**
     * 解封指定用户。
     *
     * @param admin 执行操作的管理员
     * @param userId 目标用户主键
     * @return 状态变更后的用户对象
     */
    public User unbanUser(User admin, Long userId) {
        return changeUserStatus(admin, userId, UserStatus.NORMAL, "UNBAN_USER", "解封用户");
    }

    /**
     * 统一处理用户状态切换及管理员日志记录。
     */
    private User changeUserStatus(User admin, Long userId, UserStatus targetStatus, String action, String actionName) {
        ensureAdmin(admin);
        User targetUser = userDao.findById(userId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }
        if (targetUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException("不能修改管理员账号状态");
        }
        if (targetUser.getStatus() == targetStatus) {
            throw new BusinessException("用户状态未发生变化");
        }

        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);
            userDao.updateStatus(connection, userId, targetStatus);
            adminLogService.record(connection, admin, action, "USER", userId, actionName + "：" + targetUser.getUsername());
            connection.commit();
            return userDao.findById(userId);
        } catch (BusinessException e) {
            rollbackQuietly(connection);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(connection);
            throw new IllegalStateException("更新用户状态失败", e);
        } finally {
            closeQuietly(connection);
        }
    }

    /**
     * 校验必填文本并返回去除首尾空白后的结果。
     */
    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    /**
     * 校验当前操作者是否为管理员。
     */
    private void ensureAdmin(User user) {
        if (user == null || user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("只有管理员可以执行该操作");
        }
    }

    /**
     * 尝试回滚事务，忽略回滚过程中的二次异常。
     */
    private void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * 尝试关闭数据库连接，忽略关闭过程中的二次异常。
     */
    private void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
