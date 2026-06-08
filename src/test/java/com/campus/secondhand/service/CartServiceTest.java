package com.campus.secondhand.service;

import com.campus.secondhand.config.DatabaseInitializer;
import com.campus.secondhand.model.CartItem;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.User;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CartServiceTest {

	private Path tempDatabase;

	@Before
	public void setUp() throws Exception {
		tempDatabase = Files.createTempDirectory("java-xy-cart");
		System.setProperty("SECONDHAND_DB_PATH", tempDatabase.resolve("secondhand.db").toString());
		DatabaseInitializer.initialize();
	}

	@After
	public void tearDown() {
		System.clearProperty("SECONDHAND_DB_PATH");
	}

	@Test
	public void addListRemoveAndCheckoutShouldWork() {
		UserService userService = new UserService();
		GoodsService goodsService = new GoodsService();
		CartService cartService = new CartService();

		User seller = userService.login("demo_user", "user123");
		User buyer = userService.login("demo_buyer", "buyer123");
		User admin = userService.login("admin", "admin123");

		Goods first = goodsService.publishGoods(seller, "购物车教材 A", "教材", 60.0, 30.0, 4, "适合批量结算测试");
		Goods second = goodsService.publishGoods(seller, "购物车教材 B", "教材", 70.0, 35.0, 5, "适合批量结算测试");
		goodsService.approveGoods(admin, first.getId());
		goodsService.approveGoods(admin, second.getId());

		CartItem firstItem = cartService.addToCart(buyer, first.getId());
		CartItem secondItem = cartService.addToCart(buyer, second.getId());
		Assert.assertEquals(2, cartService.listCartItems(buyer).size());

		cartService.removeFromCart(buyer, firstItem.getId());
		List<CartItem> afterRemove = cartService.listCartItems(buyer);
		Assert.assertEquals(1, afterRemove.size());
		Assert.assertEquals(second.getId(), afterRemove.get(0).getGoodsId());

		cartService.addToCart(buyer, first.getId());
		List<CartItem> checkoutItems = cartService.listCartItems(buyer);
		List<Order> orders = cartService.checkout(buyer,
				Arrays.asList(checkoutItems.get(0).getId(), checkoutItems.get(1).getId()));

		Assert.assertEquals(2, orders.size());
		Assert.assertTrue(cartService.listCartItems(buyer).isEmpty());
	}

	@Test(expected = BusinessException.class)
	public void duplicateCartItemShouldFail() {
		UserService userService = new UserService();
		CartService cartService = new CartService();
		User buyer = userService.login("demo_buyer", "buyer123");

		cartService.addToCart(buyer, 1L);
		cartService.addToCart(buyer, 1L);
	}

	@Test(expected = BusinessException.class)
	public void sellerCannotAddOwnGoodsToCart() {
		UserService userService = new UserService();
		CartService cartService = new CartService();
		User seller = userService.login("demo_user", "user123");

		cartService.addToCart(seller, 1L);
	}

	@Test(expected = BusinessException.class)
	public void soldGoodsCannotBeAddedToCart() {
		UserService userService = new UserService();
		CartService cartService = new CartService();
		User buyer = userService.login("demo_buyer", "buyer123");

		cartService.addToCart(buyer, 4L);
	}
}
