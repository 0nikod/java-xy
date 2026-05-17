package com.campus.secondhand.service;

import com.campus.secondhand.dao.UserDao;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserRole;
import com.campus.secondhand.model.UserStatus;
import com.campus.secondhand.util.DateUtil;
import com.campus.secondhand.util.PasswordUtil;

public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

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

    public User getById(Long id) {
        return userDao.findById(id);
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }
}
