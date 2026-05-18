package com.campus.secondhand.config;

import com.campus.secondhand.util.DBUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Assert;
import org.junit.Test;

/**
 * 验证数据库初始化会创建数据文件、写入种子数据并完成表结构升级。
 */
public class DatabaseInitializerTest {

	@Test
	public void initializeShouldCreateDatabaseAndSeedData() throws Exception {
		// 使用临时数据库，避免影响本地开发环境。
		Path tempDir = Files.createTempDirectory("java-xy-db-test");
		Path dbPath = tempDir.resolve("secondhand.db");
		System.setProperty("SECONDHAND_DB_PATH", dbPath.toString());

		try {
			DatabaseInitializer.initialize();

			// 初始化后应生成数据库文件。
			Assert.assertTrue(Files.exists(dbPath));

			// 验证核心表已经被种子数据填充。
			try (Connection connection = DBUtil.getConnection();
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM users")) {
				Assert.assertTrue(resultSet.next());
				Assert.assertEquals(3, resultSet.getInt("total"));
			}

			// goods 表应包含预期数量的初始商品。
			try (Connection connection = DBUtil.getConnection();
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM goods")) {
				Assert.assertTrue(resultSet.next());
				Assert.assertEquals(4, resultSet.getInt("total"));
			}

			// orders 表也应有初始化数据，确保流程测试可直接运行。
			try (Connection connection = DBUtil.getConnection();
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM orders")) {
				Assert.assertTrue(resultSet.next());
				Assert.assertEquals(1, resultSet.getInt("total"));
			}

			// 校验旧字段已被移除，避免表结构回退。
			try (Connection connection = DBUtil.getConnection();
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("PRAGMA table_info(goods)")) {
				boolean hasRejectReason = false;
				while (resultSet.next()) {
					if ("reject_reason".equals(resultSet.getString("name"))) {
						hasRejectReason = true;
					}
				}
				Assert.assertFalse("goods 表不应再包含 reject_reason 列", hasRejectReason);
			}

			// reviews 表应保留用于后续功能扩展的字段。
			try (Connection connection = DBUtil.getConnection();
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("PRAGMA table_info(reviews)")) {
				boolean hasRating = false;
				boolean hasContent = false;
				while (resultSet.next()) {
					if ("rating".equals(resultSet.getString("name"))) {
						hasRating = true;
					}
					if ("content".equals(resultSet.getString("name"))) {
						hasContent = true;
					}
				}
				Assert.assertTrue("reviews 表应预留 rating 字段", hasRating);
				Assert.assertTrue("reviews 表应预留 content 字段", hasContent);
			}
		} finally {
			System.clearProperty("SECONDHAND_DB_PATH");
		}
	}
}
