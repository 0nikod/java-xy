package com.campus.secondhand.service;

import com.campus.secondhand.config.DatabaseInitializer;
import com.campus.secondhand.dao.GoodsDao;
import com.campus.secondhand.dao.OrderDao;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsSortOption;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.User;
import com.campus.secondhand.model.UserRole;
import com.campus.secondhand.model.UserStatus;
import com.campus.secondhand.util.DBUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Stage2FlowTest {

    private Path tempDatabase;

    @Before
    public void setUp() throws Exception {
        tempDatabase = Files.createTempDirectory("java-xy-stage2");
        System.setProperty("SECONDHAND_DB_PATH", tempDatabase.resolve("secondhand.db").toString());
        DatabaseInitializer.initialize();
    }

    @After
    public void tearDown() {
        System.clearProperty("SECONDHAND_DB_PATH");
    }

    @Test
    public void registerAndLoginShouldWork() {
        UserService userService = new UserService();
        User user = userService.register("alice", "alice123", "13800138000");

        Assert.assertNotNull(user.getId());
        Assert.assertEquals(UserRole.USER, user.getRole());
        Assert.assertEquals(UserStatus.NORMAL, user.getStatus());

        User loginUser = userService.login("alice", "alice123");
        Assert.assertEquals(user.getUsername(), loginUser.getUsername());
    }

    @Test(expected = BusinessException.class)
    public void bannedUserShouldNotLogin() throws SQLException {
        UserService userService = new UserService();
        userService.register("banned_user", "secret123", "13800138001");

        try (Connection connection = DBUtil.getConnection();
                PreparedStatement ps = connection
                        .prepareStatement("UPDATE users SET status = 'BANNED' WHERE username = ?")) {
            ps.setString(1, "banned_user");
            ps.executeUpdate();
        }

        userService.login("banned_user", "secret123");
    }

    @Test
    public void publishApproveAndPurchaseFlowShouldPersistOrder() {
        UserService userService = new UserService();
        GoodsService goodsService = new GoodsService();
        OrderService orderService = new OrderService();

        User seller = userService.login("demo_user", "user123");
        User buyer = userService.register("buyer", "buyer123", "13800138002");
        User admin = userService.login("admin", "admin123");

        Goods published = goodsService.publishGoods(seller, "中型棉花娃娃", "棉花娃娃", 209.0, 189.0, 9, "20 cm 棉花娃娃");
        Assert.assertEquals(GoodsStatus.PENDING, goodsService.getGoodsDetail(published.getId()).getStatus());

        goodsService.approveGoods(admin, published.getId());
        Goods approved = goodsService.getGoodsDetail(published.getId());
        Assert.assertEquals(GoodsStatus.ON_SALE, approved.getStatus());

        Assert.assertTrue(goodsService.listPublicGoods("中型棉花娃娃", null, GoodsSortOption.LATEST)
                .stream()
                .anyMatch(goods -> goods.getId().equals(published.getId())));

        Order order = orderService.purchaseGoods(buyer, published.getId());
        Assert.assertNotNull(order.getId());
        Assert.assertEquals(GoodsStatus.SOLD, goodsService.getGoodsDetail(published.getId()).getStatus());

        Order reloadedOrder = new OrderDao().findByOrderNo(order.getOrderNo());
        Assert.assertNotNull(reloadedOrder);
        Assert.assertEquals(order.getOrderNo(), reloadedOrder.getOrderNo());
        Assert.assertEquals("中型棉花娃娃", reloadedOrder.getGoodsTitle());
    }

    @Test
    public void rejectedGoodsShouldKeepReason() {
        UserService userService = new UserService();
        GoodsService goodsService = new GoodsService();

        User seller = userService.login("demo_user", "user123");
        User admin = userService.login("admin", "admin123");

        Goods published = goodsService.publishGoods(seller, "坏掉的中型棉花娃娃", "棉花娃娃", 209.0, 189.0, 9, "这期拉了");
        goodsService.rejectGoods(admin, published.getId(), "图片不清晰");

        Goods rejected = goodsService.getGoodsDetail(published.getId());
        Assert.assertEquals(GoodsStatus.REJECTED, rejected.getStatus());
        Assert.assertEquals("图片不清晰", rejected.getRejectReason());
    }
}
