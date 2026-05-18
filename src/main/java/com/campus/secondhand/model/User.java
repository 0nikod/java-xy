package com.campus.secondhand.model;

/**
 * 用户实体，承载登录、权限、状态和基础资料信息。
 */
public class User {

	private Long id;
	private String username;
	private String passwordHash;
	private String phone;
	private UserRole role;
	private UserStatus status;
	private String createdAt;

	/**
	 * 创建空的用户对象。
	 */
	public User() {
	}

	/**
	 * 创建完整用户对象。
	 */
	public User(Long id, String username, String passwordHash, String phone, UserRole role, UserStatus status,
			String createdAt) {
		this.id = id;
		this.username = username;
		this.passwordHash = passwordHash;
		this.phone = phone;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * 获取角色的文本表示。
	 */
	public String getRoleText() {
		return role == null ? "" : role.name();
	}

	/**
	 * 获取状态的文本表示。
	 */
	public String getStatusText() {
		return status == null ? "" : status.name();
	}
}
