package com.campus.secondhand.config;

import com.campus.secondhand.util.DBUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Assert;
import org.junit.Test;

public class DatabaseInitializerTest {

    @Test
    public void initializeShouldCreateDatabaseAndSeedData() throws Exception {
        Path tempDir = Files.createTempDirectory("java-xy-db-test");
        Path dbPath = tempDir.resolve("secondhand.db");
        Path imageDir = tempDir.resolve("images");
        System.setProperty("SECONDHAND_DB_PATH", dbPath.toString());
        System.setProperty("SECONDHAND_IMAGE_DIR", imageDir.toString());

        try {
            DatabaseInitializer.initialize();

            Assert.assertTrue(Files.exists(dbPath));
            Assert.assertTrue(Files.exists(imageDir.resolve("goods-1").resolve("01_seed-book.png")));

            try (Connection connection = DBUtil.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM users")) {
                Assert.assertTrue(resultSet.next());
                Assert.assertEquals(3, resultSet.getInt("total"));
            }

            try (Connection connection = DBUtil.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM goods")) {
                Assert.assertTrue(resultSet.next());
                Assert.assertEquals(4, resultSet.getInt("total"));
            }

            try (Connection connection = DBUtil.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM orders")) {
                Assert.assertTrue(resultSet.next());
                Assert.assertEquals(1, resultSet.getInt("total"));
            }

            try (Connection connection = DBUtil.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT image_path FROM goods_images WHERE goods_id = 1")) {
                Assert.assertTrue(resultSet.next());
                Assert.assertTrue(resultSet.getString("image_path").endsWith("01_seed-book.png"));
            }

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
            System.clearProperty("SECONDHAND_IMAGE_DIR");
        }
    }
}
