package com.campus.secondhand.service;

import com.campus.secondhand.config.DatabaseInitializer;
import com.campus.secondhand.model.AdminLog;
import com.campus.secondhand.dao.OrderDao;
import com.campus.secondhand.model.AiSearchAssistResult;
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
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 覆盖第二阶段核心业务流：注册、登录、发布、审核、购买和违规删除。
 */
public class Stage2FlowTest {

	private Path tempDatabase;

	@Before
	public void setUp() throws Exception {
		// 每个测试都使用独立数据库，确保流程之间互不影响。
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
		// 验证注册后用户可立即登录。
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
		// 被封禁用户应无法通过登录校验。
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
		// 完整覆盖发布、审核、上架和下单链路。
		UserService userService = new UserService();
		GoodsService goodsService = new GoodsService();
		OrderService orderService = new OrderService();

		User seller = userService.login("demo_user", "user123");
		User buyer = userService.register("buyer", "buyer123", "13800138002");
		User admin = userService.login("admin", "admin123");

		Goods published = goodsService.publishGoods(seller, "Java 课程教材", "教材", 89.0, 45.0, 9, "课堂配套教材，少量笔记");
		Assert.assertEquals(GoodsStatus.PENDING, goodsService.getGoodsDetail(published.getId()).getStatus());

		goodsService.approveGoods(admin, published.getId());
		Goods approved = goodsService.getGoodsDetail(published.getId());
		Assert.assertEquals(GoodsStatus.ON_SALE, approved.getStatus());

		Assert.assertTrue(goodsService.listPublicGoods("Java 课程教材", null, GoodsSortOption.LATEST).stream()
				.anyMatch(goods -> goods.getId().equals(published.getId())));

		Order order = orderService.purchaseGoods(buyer, published.getId());
		Assert.assertNotNull(order.getId());
		Assert.assertEquals(GoodsStatus.SOLD, goodsService.getGoodsDetail(published.getId()).getStatus());

		Order reloadedOrder = new OrderDao().findByOrderNo(order.getOrderNo());
		Assert.assertNotNull(reloadedOrder);
		Assert.assertEquals(order.getOrderNo(), reloadedOrder.getOrderNo());
		Assert.assertEquals("Java 课程教材", reloadedOrder.getGoodsTitle());
	}

	@Test
	public void aiSearchAssistShouldProduceSafeSearchCriteria() {
		GoodsService goodsService = new GoodsService();

		AiSearchAssistResult result = goodsService.assistSearchToCriteria("我想买一本便宜点的 Java 教材，最好新一点");

		Assert.assertTrue(result.getKeyword().contains("Java"));
		Assert.assertEquals("教材", result.getCategory());
		Assert.assertEquals(GoodsSortOption.PRICE_ASC, result.getSortOption());
		Assert.assertTrue(result.getExplanation().contains("关键词"));
	}

	@Test
	public void adminShouldDeleteViolationGoodsWithReason() {
		// 违规商品删除后应从详情页消失，并留下后台日志。
		UserService userService = new UserService();
		GoodsService goodsService = new GoodsService();
		AdminLogService adminLogService = new AdminLogService();

		User seller = userService.login("demo_user", "user123");
		User admin = userService.login("admin", "admin123");

		Goods published = goodsService.publishGoods(seller, "损坏的平板壳", "数码", 59.0, 19.0, 4, "边缘磨损严重");
		goodsService.deleteGoods(admin, published.getId(), "图片不清晰");

		try {
			goodsService.getGoodsDetail(published.getId());
			Assert.fail("删除后的违规商品不应继续存在");
		} catch (BusinessException expected) {
			Assert.assertEquals("商品不存在", expected.getMessage());
		}

		List<AdminLog> logs = adminLogService.listRecent();
		Assert.assertTrue(logs.stream()
				.anyMatch(log -> "DELETE_GOODS".equals(log.getAction()) && log.getDetail().contains("图片不清晰")));
	}
}
