package com.campus.secondhand.service;

import com.campus.secondhand.dao.GoodsDao;
import com.campus.secondhand.dao.OrderDao;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.model.Order;
import com.campus.secondhand.model.OrderStatus;
import com.campus.secondhand.model.User;
import com.campus.secondhand.util.DateUtil;
import com.campus.secondhand.util.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class OrderService {

    private final GoodsDao goodsDao;
    private final OrderDao orderDao;

    public OrderService() {
        this.goodsDao = new GoodsDao();
        this.orderDao = new OrderDao();
    }

    public Order purchaseGoods(User buyer, Long goodsId) {
        // 购买流程必须在一个事务中完成：检查商品、更新状态、创建订单，任何一步失败都回滚。
        if (buyer == null) {
            throw new BusinessException("请先登录");
        }
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            Goods goods = goodsDao.loadById(connection, goodsId);
            if (goods == null) {
                throw new BusinessException("商品不存在");
            }
            if (goods.getStatus() != GoodsStatus.ON_SALE) {
                throw new BusinessException("只有在售商品才能购买");
            }
            if (goods.getSellerId().equals(buyer.getId())) {
                throw new BusinessException("不能购买自己发布的商品");
            }
            if (goods.getSellerStatus() != null && !"NORMAL".equals(goods.getSellerStatus())) {
                throw new BusinessException("卖家已被封禁，商品不可购买");
            }

            goodsDao.markSold(connection, goodsId);

            // 订单号由时间戳+随机片段组成，兼顾可读性和基本唯一性。
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setGoodsId(goods.getId());
            order.setGoodsTitle(goods.getTitle());
            order.setBuyerId(buyer.getId());
            order.setSellerId(goods.getSellerId());
            order.setDealPrice(goods.getCurrentPrice());
            order.setStatus(OrderStatus.FINISHED);
            order.setCreatedAt(DateUtil.nowText());
            orderDao.insert(connection, order);

            connection.commit();
            return order;
        } catch (BusinessException e) {
            rollbackQuietly(connection);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(connection);
            throw new IllegalStateException("购买商品失败", e);
        } finally {
            closeQuietly(connection);
        }
    }

    public List<Order> listPurchasedOrders(User buyer) {
        // 个人中心“我的购买”列表。
        if (buyer == null) {
            throw new BusinessException("请先登录");
        }
        return orderDao.listByBuyerId(buyer.getId());
    }

    public List<Order> listSoldOrders(User seller) {
        // 个人中心“我的卖出”列表。
        if (seller == null) {
            throw new BusinessException("请先登录");
        }
        return orderDao.listBySellerId(seller.getId());
    }

    private String generateOrderNo() {
        // 订单号格式固定，便于日志、人眼排查和前端展示。
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
