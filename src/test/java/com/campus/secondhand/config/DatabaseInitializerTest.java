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
        System.setProperty("SECONDHAND_DB_PATH", dbPath.toString());

        try {
            DatabaseInitializer.initialize();

            Assert.assertTrue(Files.exists(dbPath));

            try (Connection connection = DBUtil.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM users")) {
                Assert.assertTrue(resultSet.next());
                Assert.assertEquals(2, resultSet.getInt("total"));
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
        } finally {
            System.clearProperty("SECONDHAND_DB_PATH");
        }
    }
}
