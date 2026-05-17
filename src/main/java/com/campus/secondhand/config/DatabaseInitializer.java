package com.campus.secondhand.config;

import com.campus.secondhand.util.DBUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final String SCHEMA_RESOURCE = "/db/schema.sql";
    private static final String SEED_RESOURCE = "/db/seed.sql";

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try {
            DBUtil.ensureDatabaseReady();
            executeScript(SCHEMA_RESOURCE);
            executeScript(SEED_RESOURCE);
        } catch (IOException e) {
            throw new IllegalStateException("读取数据库脚本失败", e);
        } catch (SQLException e) {
            throw new IllegalStateException("初始化数据库失败", e);
        }
    }

    private static void executeScript(String resourcePath) throws IOException, SQLException {
        InputStream inputStream = DatabaseInitializer.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("找不到数据库脚本: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                builder.append(line).append('\n');
            }
            runStatements(builder.toString());
        }
    }

    private static void runStatements(String script) throws SQLException {
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement()) {
            String[] statements = script.split(";");
            for (String rawStatement : statements) {
                String sql = rawStatement.trim();
                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
        }
    }
}
