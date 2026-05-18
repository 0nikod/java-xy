package com.campus.secondhand.dao;

import com.campus.secondhand.model.GoodsImage;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoodsImageDao {

    public List<GoodsImage> listByGoodsId(Long goodsId) {
        String sql = "SELECT * FROM goods_images WHERE goods_id = ? ORDER BY display_order ASC, id ASC";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, goodsId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapList(rs);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询商品图片失败", e);
        }
    }

    public void replaceImages(Connection connection, Long goodsId, List<GoodsImage> images) throws SQLException {
        deleteByGoodsId(connection, goodsId);
        String sql = "INSERT INTO goods_images (goods_id, image_path, is_primary, display_order) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (GoodsImage image : images) {
                ps.setLong(1, goodsId);
                ps.setString(2, image.getImagePath());
                ps.setInt(3, image.isPrimary() ? 1 : 0);
                ps.setInt(4, image.getDisplayOrder());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void deleteByGoodsId(Connection connection, Long goodsId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM goods_images WHERE goods_id = ?")) {
            ps.setLong(1, goodsId);
            ps.executeUpdate();
        }
    }

    private List<GoodsImage> mapList(ResultSet rs) throws SQLException {
        List<GoodsImage> images = new ArrayList<GoodsImage>();
        while (rs.next()) {
            GoodsImage image = new GoodsImage();
            image.setId(rs.getLong("id"));
            image.setGoodsId(rs.getLong("goods_id"));
            image.setImagePath(rs.getString("image_path"));
            image.setPrimary(rs.getInt("is_primary") == 1);
            image.setDisplayOrder(rs.getInt("display_order"));
            images.add(image);
        }
        return images;
    }
}
