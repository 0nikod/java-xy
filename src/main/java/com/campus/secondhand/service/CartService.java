package com.campus.secondhand.service;

import com.campus.secondhand.dao.CartDao;
import com.campus.secondhand.dao.GoodsDao;
import com.campus.secondhand.dao.OrderDao;
import com.campus.secondhand.model.CartItem;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.OrderStatus;
import com.campus.secondhand.model.User;
import com.campus.secondhand.util.DBUtil;
import com.campus.secondhand.util.DateUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartService {

	private final CartDao cartDao;
	private final GoodsDao goodsDao;
	private final OrderDao orderDao;

	public CartService() {
		this.cartDao = new CartDao();
		this.goodsDao = new GoodsDao();
		this.orderDao = new OrderDao();
	}

	public CartItem addToCart(User user, Long goodsId) {
		ensureUser(user);
		Goods goods = goodsDao.findById(goodsId);
		validatePurchasable(user, goods);
		if (cartDao.findByUserAndGoods(user.getId(), goodsId) != null) {
			throw new BusinessException("该商品已在购物车中");
		}
		CartItem item = new CartItem();
		item.setUserId(user.getId());
		item.setGoodsId(goodsId);
		item.setCreatedAt(DateUtil.nowText());
		cartDao.insert(item);
		return cartDao.findById(item.getId());
	}

	public List<CartItem> listCartItems(User user) {
		ensureUser(user);
		return cartDao.listByUserId(user.getId());
	}

	public void removeFromCart(User user, Long cartItemId) {
		ensureUser(user);
		if (cartDao.deleteByIdAndUser(cartItemId, user.getId()) == 0) {
			throw new BusinessException("购物车商品不存在");
		}
	}

	public List<Order> checkout(User user, List<Long> cartItemIds) {
		ensureUser(user);
		if (cartItemIds == null || cartItemIds.isEmpty()) {
			throw new BusinessException("请选择要结算的商品");
		}
		Connection connection = null;
		try {
			connection = DBUtil.getConnection();
			connection.setAutoCommit(false);
			List<Order> orders = new ArrayList<Order>();
			for (Long cartItemId : cartItemIds) {
				CartItem item = cartDao.findById(cartItemId);
				if (item == null || !user.getId().equals(item.getUserId())) {
					throw new BusinessException("购物车商品不存在或无权结算");
				}
				Goods goods = goodsDao.loadById(connection, item.getGoodsId());
				validatePurchasable(user, goods);
				goodsDao.markSold(connection, goods.getId());
				Order order = buildOrder(user, goods);
				orderDao.insert(connection, order);
				// 购物车结算成交后同步清除所有用户购物车中的同一商品，避免留下已售商品。
				cartDao.deleteByGoodsId(connection, goods.getId());
				orders.add(order);
			}
			cartDao.deleteByIds(connection, user.getId(), cartItemIds);
			connection.commit();
			return orders;
		} catch (BusinessException e) {
			rollbackQuietly(connection);
			throw e;
		} catch (Exception e) {
			rollbackQuietly(connection);
			throw new IllegalStateException("购物车结算失败", e);
		} finally {
			closeQuietly(connection);
		}
	}

	private Order buildOrder(User buyer, Goods goods) {
		Order order = new Order();
		order.setOrderNo(generateOrderNo());
		order.setGoodsId(goods.getId());
		order.setGoodsTitle(goods.getTitle());
		order.setBuyerId(buyer.getId());
		order.setSellerId(goods.getSellerId());
		order.setDealPrice(goods.getCurrentPrice());
		order.setStatus(OrderStatus.FINISHED);
		order.setCreatedAt(DateUtil.nowText());
		return order;
	}

	private void validatePurchasable(User buyer, Goods goods) {
		if (goods == null) {
			throw new BusinessException("商品不存在");
		}
		if (goods.getStatus() != GoodsStatus.ON_SALE) {
			throw new BusinessException("只有在售商品才能加入购物车或结算");
		}
		if (goods.getSellerId().equals(buyer.getId())) {
			throw new BusinessException("不能购买自己发布的商品");
		}
		if (goods.getSellerStatus() != null && !"NORMAL".equals(goods.getSellerStatus())) {
			throw new BusinessException("卖家已被封禁，商品不可购买");
		}
	}

	private void ensureUser(User user) {
		if (user == null) {
			throw new BusinessException("请先登录");
		}
	}

	private String generateOrderNo() {
		String timePart = DateUtil.nowText().replace("-", "").replace(":", "").replace(" ", "");
		String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
		return "ORD-" + timePart + "-" + randomPart;
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
}
