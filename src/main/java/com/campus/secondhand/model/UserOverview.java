package com.campus.secondhand.model;

/**
 * 用户概览实体，用于后台用户列表和统计展示。
 */
public class UserOverview {

    private Long id;
    private String username;
    private String phone;
    private String role;
    private String status;
    private int publishedGoodsCount;
    private int soldOrderCount;
    private String createdAt;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPublishedGoodsCount() {
        return publishedGoodsCount;
    }

    public void setPublishedGoodsCount(int publishedGoodsCount) {
        this.publishedGoodsCount = publishedGoodsCount;
    }

    public int getSoldOrderCount() {
        return soldOrderCount;
    }

    public void setSoldOrderCount(int soldOrderCount) {
        this.soldOrderCount = soldOrderCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
