package com.campus.secondhand.service;

import com.campus.secondhand.dao.GoodsDao;
import com.campus.secondhand.dao.GoodsImageDao;
import com.campus.secondhand.model.GoodsImage;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsCategory;
import com.campus.secondhand.model.GoodsSortOption;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.model.User;
import com.campus.secondhand.util.DateUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GoodsService {

    private final GoodsDao goodsDao;
    private final AdminLogService adminLogService;
    private final AiService aiService;
    private final GoodsImageDao goodsImageDao;
    private final ImageStorageService imageStorageService;

    public GoodsService() {
        this.goodsDao = new GoodsDao();
        this.adminLogService = new AdminLogService();
        this.aiService = AiServiceFactory.create();
        this.goodsImageDao = new GoodsImageDao();
        this.imageStorageService = new ImageStorageService();
    }

    public Goods publishGoods(User seller, String title, String category, double originalPrice, double currentPrice,
            int conditionLevel, String description) {
        assertSeller(seller);
        String normalizedTitle = normalizeRequired(title, "商品名称不能为空");
        String normalizedCategory = normalizeRequired(category, "商品分类不能为空");
        String normalizedDescription = normalizeRequired(description, "商品描述不能为空");
        validateCategory(normalizedCategory);

        if (originalPrice <= 0) {
            throw new BusinessException("原价必须大于 0");
        }
        if (currentPrice <= 0) {
            throw new BusinessException("现价必须大于 0");
        }
        if (conditionLevel < 1 || conditionLevel > 10) {
            throw new BusinessException("新旧程度必须在 1 到 10 之间");
        }

        Goods goods = new Goods();
        goods.setSellerId(seller.getId());
        goods.setTitle(normalizedTitle);
        goods.setCategory(normalizedCategory);
        goods.setOriginalPrice(originalPrice);
        goods.setCurrentPrice(currentPrice);
        goods.setConditionLevel(conditionLevel);
        goods.setDescription(normalizedDescription);
        goods.setStatus(GoodsStatus.PENDING);
        goods.setCreatedAt(DateUtil.nowText());
        goods.setUpdatedAt(goods.getCreatedAt());
        goodsDao.insert(goods);
        return goods;
    }

    public List<Goods> listPublicGoods(String keyword, String category, GoodsSortOption sortOption) {
        return goodsDao.listPublicGoods(keyword, category, sortOption);
    }

    public List<Goods> listPendingGoods() {
        return goodsDao.listPendingGoods();
    }

    public Goods getGoodsDetail(Long goodsId) {
        Goods goods = goodsDao.findById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        return goods;
    }

    public List<Goods> listUserPublishedGoods(User user) {
        assertSeller(user);
        return goodsDao.listUserPublishedGoods(user.getId());
    }

    public Goods approveGoods(User admin, Long goodsId) {
        ensureAdmin(admin);
        Goods goods = getGoodsDetail(goodsId);
        if (goods.getStatus() != GoodsStatus.PENDING) {
            throw new BusinessException("只有待审核商品才能审核通过");
        }
        goodsDao.updateStatus(goodsId, GoodsStatus.ON_SALE);
        recordAdminLog(admin, "APPROVE_GOODS", "GOODS", goodsId, "审核通过商品：" + goods.getTitle());
        return getGoodsDetail(goodsId);
    }

    public Goods offShelfGoods(User seller, Long goodsId) {
        assertSeller(seller);
        Goods goods = getGoodsDetail(goodsId);
        if (!goods.getSellerId().equals(seller.getId())) {
            throw new BusinessException("只能下架自己发布的商品");
        }
        if (goods.getStatus() == GoodsStatus.SOLD) {
            throw new BusinessException("已售出商品不能下架");
        }
        goodsDao.updateStatus(goodsId, GoodsStatus.OFF_SHELF);
        return getGoodsDetail(goodsId);
    }

    public List<GoodsImage> listGoodsImages(Long goodsId) {
        getGoodsDetail(goodsId);
        return goodsImageDao.listByGoodsId(goodsId);
    }

    public void saveGoodsImages(User seller, Long goodsId, List<Path> sourcePaths, int primaryIndex) {
        assertSeller(seller);
        Goods goods = getGoodsDetail(goodsId);
        if (!goods.getSellerId().equals(seller.getId())) {
            throw new BusinessException("只能管理自己发布商品的图片");
        }
        if (sourcePaths == null) {
            sourcePaths = new ArrayList<Path>();
        }
        if (sourcePaths.size() > 3) {
            throw new BusinessException("商品图片最多上传 3 张");
        }
        if (!sourcePaths.isEmpty() && (primaryIndex < 0 || primaryIndex >= sourcePaths.size())) {
            throw new BusinessException("请选择有效的主图");
        }

        Connection connection = null;
        try {
            connection = com.campus.secondhand.util.DBUtil.getConnection();
            connection.setAutoCommit(false);
            imageStorageService.deleteGoodsImages(goodsId);
            List<String> storedPaths = imageStorageService.storeGoodsImages(goodsId, sourcePaths);
            List<GoodsImage> images = new ArrayList<GoodsImage>();
            for (int i = 0; i < storedPaths.size(); i++) {
                GoodsImage image = new GoodsImage();
                image.setGoodsId(goodsId);
                image.setImagePath(storedPaths.get(i));
                image.setPrimary(i == primaryIndex);
                image.setDisplayOrder(i);
                images.add(image);
            }
            goodsImageDao.replaceImages(connection, goodsId, images);
            connection.commit();
        } catch (BusinessException e) {
            rollbackQuietly(connection);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(connection);
            throw new IllegalStateException("保存商品图片失败", e);
        } finally {
            closeQuietly(connection);
        }
    }

    public void deleteGoods(User admin, Long goodsId, String reason) {
        ensureAdmin(admin);
        Goods goods = getGoodsDetail(goodsId);
        String normalizedReason = normalizeRequired(reason, "删除原因不能为空");
        Connection connection = null;
        try {
            connection = com.campus.secondhand.util.DBUtil.getConnection();
            connection.setAutoCommit(false);
            goodsImageDao.deleteByGoodsId(connection, goodsId);
            goodsDao.deleteById(connection, goodsId);
            adminLogService.record(connection, admin, "DELETE_GOODS", "GOODS", goodsId,
                    "删除违规商品：" + goods.getTitle() + "，原因：" + normalizedReason);
            connection.commit();
            imageStorageService.deleteGoodsImages(goodsId);
        } catch (BusinessException e) {
            rollbackQuietly(connection);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(connection);
            throw new IllegalStateException("删除商品失败", e);
        } finally {
            closeQuietly(connection);
        }
    }

    public String optimizeDescription(String description) {
        return aiService.optimizeDescription(description);
    }

    public String buildOperationsSummary() {
        return aiService.buildOperationsSummary("请生成一段适合管理员首页展示的通用运营摘要。");
    }

    private void recordAdminLog(User admin, String action, String targetType, Long targetId, String detail) {
        try (Connection connection = com.campus.secondhand.util.DBUtil.getConnection()) {
            adminLogService.record(connection, admin, action, targetType, targetId, detail);
        } catch (SQLException e) {
            throw new IllegalStateException("记录管理员日志失败", e);
        }
    }

    private void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void ensureAdmin(User user) {
        if (user == null || user.getRole() == null || !"ADMIN".equals(user.getRole().name())) {
            throw new BusinessException("只有管理员可以执行该操作");
        }
    }

    private void assertSeller(User user) {
        if (user == null) {
            throw new BusinessException("请先登录");
        }
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private void validateCategory(String category) {
        for (GoodsCategory value : GoodsCategory.values()) {
            if (value.getDisplayName().equals(category)) {
                return;
            }
        }
        throw new BusinessException("商品分类不合法");
    }
}
