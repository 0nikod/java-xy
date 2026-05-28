package com.campus.secondhand.service;

import com.campus.secondhand.dao.GoodsDao;
import com.campus.secondhand.dao.OrderDao;
import com.campus.secondhand.dao.ReviewDao;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.OrderStatus;
import com.campus.secondhand.model.Review;
import com.campus.secondhand.model.User;
import com.campus.secondhand.util.DateUtil;
import java.util.List;

public class ReviewService {

	private final ReviewDao reviewDao;
	private final OrderDao orderDao;
	private final GoodsDao goodsDao;

	public ReviewService() {
		this.reviewDao = new ReviewDao();
		this.orderDao = new OrderDao();
		this.goodsDao = new GoodsDao();
	}

	public Review createReview(User reviewer, Long orderId, int rating, String content) {
		ensureUser(reviewer);
		Order order = orderDao.findById(orderId);
		if (order == null) {
			throw new BusinessException("订单不存在");
		}
		if (!order.getBuyerId().equals(reviewer.getId())) {
			throw new BusinessException("只能评价自己购买的订单");
		}
		if (order.getStatus() != OrderStatus.FINISHED) {
			throw new BusinessException("只能评价已完成订单");
		}
		if (reviewDao.findByOrderId(orderId) != null) {
			throw new BusinessException("该订单已评价");
		}
		if (rating < 1 || rating > 5) {
			throw new BusinessException("评分必须在 1 到 5 之间");
		}
		String normalizedContent = normalizeRequired(content, "评价内容不能为空");
		Review review = new Review();
		review.setOrderId(order.getId());
		review.setGoodsId(order.getGoodsId());
		review.setReviewerId(reviewer.getId());
		review.setSellerId(order.getSellerId());
		review.setRating(rating);
		review.setContent(normalizedContent);
		review.setCreatedAt(DateUtil.nowText());
		reviewDao.insert(review);
		return reviewDao.findByOrderId(orderId);
	}

	public List<Review> listGoodsReviews(Long goodsId) {
		Goods goods = goodsDao.findById(goodsId);
		if (goods == null) {
			throw new BusinessException("商品不存在");
		}
		return reviewDao.listByGoodsId(goodsId);
	}

	public List<Review> listPublishedReviews(User reviewer) {
		ensureUser(reviewer);
		return reviewDao.listByReviewerId(reviewer.getId());
	}

	public List<Review> listReceivedReviews(User seller) {
		ensureUser(seller);
		return reviewDao.listBySellerId(seller.getId());
	}

	private void ensureUser(User user) {
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
}
