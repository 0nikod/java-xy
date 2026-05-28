package com.campus.secondhand.service;

import com.campus.secondhand.config.DatabaseInitializer;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.Review;
import com.campus.secondhand.model.User;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReviewServiceTest {

	private Path tempDatabase;

	@Before
	public void setUp() throws Exception {
		tempDatabase = Files.createTempDirectory("java-xy-review");
		System.setProperty("SECONDHAND_DB_PATH", tempDatabase.resolve("secondhand.db").toString());
		DatabaseInitializer.initialize();
	}

	@After
	public void tearDown() {
		System.clearProperty("SECONDHAND_DB_PATH");
	}

	@Test
	public void buyerShouldCreateAndQueryReview() {
		UserService userService = new UserService();
		GoodsService goodsService = new GoodsService();
		OrderService orderService = new OrderService();
		ReviewService reviewService = new ReviewService();

		User seller = userService.login("demo_user", "user123");
		User buyer = userService.login("demo_buyer", "buyer123");
		User admin = userService.login("admin", "admin123");
		Goods goods = goodsService.publishGoods(seller, "评价测试教材", "教材", 80.0, 40.0, 9, "用于评价测试");
		goodsService.approveGoods(admin, goods.getId());
		Order order = orderService.purchaseGoods(buyer, goods.getId());

		Review review = reviewService.createReview(buyer, order.getId(), 5, "商品很好，交易顺利");

		Assert.assertNotNull(review.getId());
		Assert.assertEquals(Integer.valueOf(5), review.getRating());
		Assert.assertEquals(1, reviewService.listGoodsReviews(goods.getId()).size());
		Assert.assertEquals(1, reviewService.listPublishedReviews(buyer).size());
		Assert.assertEquals(1, reviewService.listReceivedReviews(seller).size());
	}

	@Test(expected = BusinessException.class)
	public void nonBuyerCannotReviewOrder() {
		UserService userService = new UserService();
		ReviewService reviewService = new ReviewService();
		User seller = userService.login("demo_user", "user123");

		reviewService.createReview(seller, 1L, 5, "不是买家不能评价");
	}

	@Test(expected = BusinessException.class)
	public void duplicateReviewShouldFail() {
		UserService userService = new UserService();
		ReviewService reviewService = new ReviewService();
		User buyer = userService.login("demo_buyer", "buyer123");

		reviewService.createReview(buyer, 1L, 5, "第一次评价");
		reviewService.createReview(buyer, 1L, 4, "重复评价");
	}

	@Test(expected = BusinessException.class)
	public void invalidRatingShouldFail() {
		UserService userService = new UserService();
		ReviewService reviewService = new ReviewService();
		User buyer = userService.login("demo_buyer", "buyer123");

		reviewService.createReview(buyer, 1L, 6, "评分错误");
	}

	@Test(expected = BusinessException.class)
	public void emptyContentShouldFail() {
		UserService userService = new UserService();
		ReviewService reviewService = new ReviewService();
		User buyer = userService.login("demo_buyer", "buyer123");

		reviewService.createReview(buyer, 1L, 5, " ");
	}

	@Test(expected = BusinessException.class)
	public void nonAdminCannotListAllReviews() {
		UserService userService = new UserService();
		ReviewService reviewService = new ReviewService();
		User buyer = userService.login("demo_buyer", "buyer123");

		reviewService.listAllReviews(buyer);
	}
}
