package com.campus.secondhand.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 * 验证应用配置相关路径解析是否符合预期。
 */
public class AppConfigTest {

	@Test
	public void resolveDefaultPathsShouldUseRelativeRuntimeDataRoot() throws Exception {
		// 默认路径应跟随运行目录解析，便于 dist 目录直接启动。
		String originalUserDir = System.getProperty("user.dir");
		Path tempDir = Files.createTempDirectory("java-xy-runtime-root");
		System.clearProperty("SECONDHAND_DB_PATH");
		System.clearProperty("SECONDHAND_IMAGE_DIR");
		System.setProperty("user.dir", tempDir.toString());
		try {
			Assert.assertEquals(tempDir.resolve("data/secondhand.db").normalize(), AppConfig.resolveDatabasePath());
			Assert.assertEquals(tempDir.resolve("data/images").normalize(), AppConfig.resolveImageStorageRoot());
		} finally {
			System.setProperty("user.dir", originalUserDir);
		}
	}

	@Test
	public void resolveConfiguredRelativePathsShouldStillResolveAgainstRuntimeDir() throws Exception {
		// 显式相对配置也应以运行目录为基准展开。
		String originalUserDir = System.getProperty("user.dir");
		Path tempDir = Files.createTempDirectory("java-xy-configured-root");
		System.setProperty("user.dir", tempDir.toString());
		System.setProperty("SECONDHAND_DB_PATH", "data/custom.db");
		System.setProperty("SECONDHAND_IMAGE_DIR", "data/custom-images");
		try {
			Assert.assertEquals(tempDir.resolve("data/custom.db").normalize(), AppConfig.resolveDatabasePath());
			Assert.assertEquals(tempDir.resolve("data/custom-images").normalize(), AppConfig.resolveImageStorageRoot());
		} finally {
			System.clearProperty("SECONDHAND_DB_PATH");
			System.clearProperty("SECONDHAND_IMAGE_DIR");
			System.setProperty("user.dir", originalUserDir);
		}
	}
}
