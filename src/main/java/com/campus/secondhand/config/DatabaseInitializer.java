package com.campus.secondhand.config;

import com.campus.secondhand.util.DBUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

public final class DatabaseInitializer {

	private static final String SCHEMA_RESOURCE = "/db/schema.sql";
	private static final String SEED_RESOURCE = "/db/seed.sql";
	private static final String DEMO_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8Xw8AAoMBgQotM7sAAAAASUVORK5CYII=";

	private DatabaseInitializer() {
	}

	public static void initialize() {
		// 启动时先确保数据库文件可用，再依次执行结构脚本和种子脚本。
		try {
			DBUtil.ensureDatabaseReady();
			executeScript(SCHEMA_RESOURCE);
			executeScript(SEED_RESOURCE);
			ensureSeedImages();
		} catch (IOException e) {
			throw new IllegalStateException("读取数据库脚本失败", e);
		} catch (SQLException e) {
			throw new IllegalStateException("初始化数据库失败", e);
		}
	}

	private static void executeScript(String resourcePath) throws IOException, SQLException {
		// 逐行读取资源脚本，忽略空行和单行注释，便于在 SQL 文件中保留说明文字。
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
		// 采用分号切分的简化执行方式，适配当前项目的 DDL/DML 脚本。
		try (Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
			String[] statements = script.split(";");
			for (String rawStatement : statements) {
				String sql = rawStatement.trim();
				if (!sql.isEmpty()) {
					statement.execute(sql);
				}
			}
		}
	}

	private static void ensureSeedImages() throws IOException, SQLException {
		// 演示库至少准备一张真实本地图，确保首页、详情和审核页都有可展示的图片样本。
		Path goodsDir = AppConfig.resolveImageStorageRoot().resolve("goods-1");
		Files.createDirectories(goodsDir);
		Path imagePath = goodsDir.resolve("01_seed-book.png");
		if (!Files.exists(imagePath)) {
			Files.write(imagePath, Base64.getDecoder().decode(DEMO_IMAGE_BASE64));
		}

		try (Connection connection = DBUtil.getConnection();
				PreparedStatement delete = connection.prepareStatement("DELETE FROM goods_images WHERE goods_id = ?");
				PreparedStatement insert = connection.prepareStatement(
						"INSERT INTO goods_images (goods_id, image_path, is_primary, display_order) VALUES (?, ?, ?, ?)")) {
			delete.setLong(1, 1L);
			delete.executeUpdate();

			insert.setLong(1, 1L);
			insert.setString(2, imagePath.toAbsolutePath().toString());
			insert.setInt(3, 1);
			insert.setInt(4, 0);
			insert.executeUpdate();
		}
	}
}
