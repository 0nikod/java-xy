package com.campus.secondhand.config;

import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 * 验证应用配置相关路径解析是否符合预期。
 */
public class AppConfigTest {

	@Test
	public void resolveImageStorageRootShouldRespectConfiguredDirectory() {
		// 通过系统属性覆盖默认图片目录，确认配置可被外部参数接管。
		System.setProperty("SECONDHAND_IMAGE_DIR", "build/test-images");
		try {
			Path root = AppConfig.resolveImageStorageRoot();
			Assert.assertTrue(root.toString().endsWith("build/test-images"));
		} finally {
			System.clearProperty("SECONDHAND_IMAGE_DIR");
		}
	}
}
