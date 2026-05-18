package com.campus.secondhand.service;

import com.campus.secondhand.config.DatabaseInitializer;
import com.campus.secondhand.model.AdminLog;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsImage;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.StatsSummary;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserOverview;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Stage3ManagementTest {

    private Path tempDatabase;

    @Before
    public void setUp() throws Exception {
        tempDatabase = Files.createTempDirectory("java-xy-stage3");
        System.setProperty("SECONDHAND_DB_PATH", tempDatabase.resolve("secondhand.db").toString());
        DatabaseInitializer.initialize();
    }

    @After
    public void tearDown() {
        System.clearProperty("SECONDHAND_DB_PATH");
    }

    @Test
    public void userCenterDataShouldReflectPublishedPurchasedAndSoldOrders() {
        UserService userService = new UserService();
        GoodsService goodsService = new GoodsService();
        OrderService orderService = new OrderService();

        User admin = userService.login("admin", "admin123");
        User seller = userService.login("demo_user", "user123");
        User buyer = userService.register("stage3_buyer", "buyer123", "13800138003");
        int soldBefore = orderService.listSoldOrders(seller).size();
        int purchasedBefore = orderService.listPurchasedOrders(buyer).size();

        Goods goods = goodsService.publishGoods(seller, "离散数学教材", "教材", 68.0, 36.0, 8, "适合期末复习");
        goodsService.approveGoods(admin, goods.getId());
        Order order = orderService.purchaseGoods(buyer, goods.getId());

        Assert.assertEquals(purchasedBefore + 1, orderService.listPurchasedOrders(buyer).size());
        Assert.assertEquals(soldBefore + 1, orderService.listSoldOrders(seller).size());
        Assert.assertTrue(orderService.listSoldOrders(seller)
                .stream()
                .anyMatch(item -> order.getOrderNo().equals(item.getOrderNo())));
    }

    @Test
    public void adminBanAndUnbanShouldWriteLogs() {
        UserService userService = new UserService();
        AdminLogService adminLogService = new AdminLogService();

        User admin = userService.login("admin", "admin123");
        User target = userService.register("stage3_user", "user123", "13800138004");

        userService.banUser(admin, target.getId());
        User banned = userService.getById(target.getId());
        Assert.assertEquals("BANNED", banned.getStatusText());

        userService.unbanUser(admin, target.getId());
        User recovered = userService.getById(target.getId());
        Assert.assertEquals("NORMAL", recovered.getStatusText());

        List<AdminLog> logs = adminLogService.listRecent();
        Assert.assertTrue(logs.stream().anyMatch(log -> "BAN_USER".equals(log.getAction()) && log.getDetail().contains("stage3_user")));
        Assert.assertTrue(logs.stream().anyMatch(log -> "UNBAN_USER".equals(log.getAction()) && log.getDetail().contains("stage3_user")));
    }

    @Test
    public void statisticsShouldExposeSummaryChartsAndUserOverview() {
        UserService userService = new UserService();
        GoodsService goodsService = new GoodsService();
        OrderService orderService = new OrderService();
        StatisticsService statisticsService = new StatisticsService();

        User admin = userService.login("admin", "admin123");
        User seller = userService.login("demo_user", "user123");
        User buyer = userService.register("stats_buyer", "buyer123", "13800138005");

        Goods approvedGoods = goodsService.publishGoods(seller, "高数辅导书", "教材", 48.0, 25.0, 7, "刷题用书");
        goodsService.approveGoods(admin, approvedGoods.getId());
        orderService.purchaseGoods(buyer, approvedGoods.getId());

        Goods pendingGoods = goodsService.publishGoods(seller, "宿舍收纳箱", "生活用品", 30.0, 18.0, 8, "九成新");
        Assert.assertEquals(GoodsStatus.PENDING, goodsService.getGoodsDetail(pendingGoods.getId()).getStatus());

        StatsSummary summary = statisticsService.loadSummary();
        Assert.assertTrue(summary.getTotalUsers() >= 3);
        Assert.assertTrue(summary.getTotalGoods() >= 4);
        Assert.assertTrue(summary.getTotalOrders() >= 1);

        List<UserOverview> overviews = statisticsService.listUserOverviews("demo_user");
        Assert.assertEquals(1, overviews.size());
        Assert.assertTrue(overviews.get(0).getPublishedGoodsCount() >= 1);
        Assert.assertTrue(overviews.get(0).getSoldOrderCount() >= 1);

        Assert.assertFalse(statisticsService.listGoodsCategoryStats().isEmpty());
        Assert.assertFalse(statisticsService.listGoodsStatusStats().isEmpty());
        Assert.assertFalse(statisticsService.listRecentOrderTrend().isEmpty());
    }

    @Test
    public void imageStorageAndViolationDeleteShouldWork() throws Exception {
        UserService userService = new UserService();
        GoodsService goodsService = new GoodsService();
        AdminLogService adminLogService = new AdminLogService();

        User admin = userService.login("admin", "admin123");
        User seller = userService.login("demo_user", "user123");

        Goods goods = goodsService.publishGoods(seller, "图像测试教材", "教材", 66.0, 32.0, 7, "包含图片测试");

        Path imageA = tempDatabase.resolve("image-a.jpg");
        Path imageB = tempDatabase.resolve("image-b.jpg");
        Files.write(imageA, "fake-a".getBytes(StandardCharsets.UTF_8));
        Files.write(imageB, "fake-b".getBytes(StandardCharsets.UTF_8));

        goodsService.saveGoodsImages(seller, goods.getId(), java.util.Arrays.asList(imageA, imageB), 1);

        List<GoodsImage> images = goodsService.listGoodsImages(goods.getId());
        Assert.assertEquals(2, images.size());
        Assert.assertTrue(images.get(1).isPrimary());
        Assert.assertTrue(Files.exists(java.nio.file.Paths.get(images.get(0).getImagePath())));

        goodsService.deleteGoods(admin, goods.getId(), "违规测试删除");

        try {
            goodsService.getGoodsDetail(goods.getId());
            Assert.fail("删除后商品不应继续存在");
        } catch (BusinessException expected) {
            Assert.assertEquals("商品不存在", expected.getMessage());
        }

        Assert.assertTrue(adminLogService.listRecent()
                .stream()
                .anyMatch(log -> "DELETE_GOODS".equals(log.getAction()) && log.getDetail().contains("违规测试删除")));
    }
}
