package com.campus.secondhand.service;

import com.campus.secondhand.dao.CartDao;
import com.campus.secondhand.dao.GoodsDao;
import com.campus.secondhand.dao.GoodsImageDao;
import com.campus.secondhand.model.AiSearchAssistResult;
import com.campus.secondhand.model.Goods;
import com.campus.secondhand.model.GoodsCategory;
import com.campus.secondhand.model.GoodsImage;
import com.campus.secondhand.model.GoodsSortOption;
import com.campus.secondhand.model.GoodsStatus;
import com.campus.secondhand.model.User;
import com.campus.secondhand.util.DateUtil;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GoodsService {

	private final GoodsDao goodsDao;
	private final CartDao cartDao;
	private final AdminLogService adminLogService;
	private final AiService aiService;
	private final GoodsImageDao goodsImageDao;
	private final ImageStorageService imageStorageService;

	public GoodsService() {
		this.goodsDao = new GoodsDao();
		this.cartDao = new CartDao();
		this.adminLogService = new AdminLogService();
		this.aiService = AiServiceFactory.create();
		this.goodsImageDao = new GoodsImageDao();
		this.imageStorageService = new ImageStorageService();
	}

	public Goods publishGoods(User seller, String title, String category, double originalPrice, double currentPrice,
			int conditionLevel, String description) {
		// 发布前先做身份、必填项和业务范围校验，避免脏数据进入数据库。
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
		// 审核通过只允许管理员处理待审核商品，审核后切换为在售状态并写入操作日志。
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
		// 下架仅允许商品发布者操作，且已售商品不能再下架。
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
		// 图片信息依附于商品存在，先校验商品是否存在再查询图片列表。
		getGoodsDetail(goodsId);
		return goodsImageDao.listByGoodsId(goodsId);
	}

	public void saveGoodsImages(User seller, Long goodsId, List<Path> sourcePaths, int primaryIndex) {
		// 保存图片采用“先清空旧图，再落盘新图，再写数据库”的方式，避免图片与记录不一致。
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
		// 删除违规商品时要同步删除图片、商品记录并记录管理员日志，保证可追踪性。
		ensureAdmin(admin);
		Goods goods = getGoodsDetail(goodsId);
		String normalizedReason = normalizeRequired(reason, "删除原因不能为空");
		Connection connection = null;
		try {
			connection = com.campus.secondhand.util.DBUtil.getConnection();
			connection.setAutoCommit(false);
			cartDao.deleteByGoodsId(connection, goodsId);
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

	public String optimizeDescriptionStreaming(String description, Consumer<String> onDelta) {
		return aiService.optimizeDescriptionStreaming(description, onDelta);
	}

	public String buildPurchaseAdviceStreaming(Long goodsId, Consumer<String> onDelta) {
		Goods goods = getGoodsDetail(goodsId);
		return aiService.buildPurchaseAdviceStreaming(buildGoodsContext(goods), onDelta);
	}

	public String assistSearchStreaming(String userInput, Consumer<String> onDelta) {
		return aiService.assistSearchStreaming(userInput, onDelta);
	}

	public AiSearchAssistResult assistSearchToCriteria(String userInput) {
		String normalizedInput = normalizeRequired(userInput, "请输入搜索需求");
		String aiText = aiService.assistSearchStreaming(normalizedInput, null);
		return buildSearchAssistResult(normalizedInput, aiText);
	}

	private AiSearchAssistResult buildSearchAssistResult(String userInput, String aiText) {
		AiSearchAssistResult result = new AiSearchAssistResult();
		String combinedText = ((userInput == null ? "" : userInput) + " " + (aiText == null ? "" : aiText)).trim();
		result.setKeyword(limitKeyword(extractKeyword(userInput)));
		result.setCategory(extractCategory(combinedText));
		result.setSortOption(extractSortOption(combinedText));
		result.setExplanation(buildSearchExplanation(result, aiText));
		return result;
	}

	private String extractKeyword(String userInput) {
		String keyword = userInput == null ? "" : userInput.trim();
		String[] removeWords = {"我想买", "想买", "我要买", "求购", "一个", "一本", "一件", "最好", "希望", "便宜点", "便宜", "新一点", "新点", "的",
				"，", ",", "。", "！", "!"};
		for (String word : removeWords) {
			keyword = keyword.replace(word, " ");
		}
		for (GoodsCategory category : GoodsCategory.values()) {
			keyword = keyword.replace(category.getDisplayName(), " ");
		}
		return keyword.trim().replaceAll("\\s+", " ");
	}

	private String limitKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return null;
		}
		String normalized = keyword.trim();
		return normalized.length() > 40 ? normalized.substring(0, 40) : normalized;
	}

	private String extractCategory(String text) {
		if (text == null) {
			return null;
		}
		for (GoodsCategory category : GoodsCategory.values()) {
			if (text.contains(category.getDisplayName())) {
				return category.getDisplayName();
			}
		}
		return null;
	}

	private GoodsSortOption extractSortOption(String text) {
		if (text == null || text.trim().isEmpty()) {
			return GoodsSortOption.LATEST;
		}
		if (containsAny(text, "便宜", "低价", "价格低", "价格升序", "从低到高")) {
			return GoodsSortOption.PRICE_ASC;
		}
		if (containsAny(text, "贵", "高价", "价格高", "价格降序", "从高到低")) {
			return GoodsSortOption.PRICE_DESC;
		}
		if (containsAny(text, "新", "成色", "九成", "八成", "品相")) {
			return GoodsSortOption.CONDITION_DESC;
		}
		return GoodsSortOption.LATEST;
	}

	private boolean containsAny(String text, String... words) {
		for (String word : words) {
			if (text.contains(word)) {
				return true;
			}
		}
		return false;
	}

	private String buildSearchExplanation(AiSearchAssistResult result, String aiText) {
		StringBuilder builder = new StringBuilder("已根据你的需求生成搜索条件");
		if (result.getKeyword() != null) {
			builder.append("，关键词：").append(result.getKeyword());
		}
		if (result.getCategory() != null) {
			builder.append("，分类：").append(result.getCategory());
		}
		builder.append("，排序：").append(result.getSortOption() == null ? GoodsSortOption.LATEST : result.getSortOption());
		if (aiText != null && !aiText.trim().isEmpty()) {
			builder.append("。AI 建议：").append(aiText.trim());
		}
		return builder.toString();
	}

	private String buildGoodsContext(Goods goods) {
		return String.format("商品：%s；分类：%s；原价：%.2f；现价：%.2f；成色：%d/10；卖家：%s；状态：%s；描述：%s", goods.getTitle(),
				goods.getCategory(), goods.getOriginalPrice(), goods.getCurrentPrice(), goods.getConditionLevel(),
				goods.getSellerUsername(), goods.getStatusText(), goods.getDescription());
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
