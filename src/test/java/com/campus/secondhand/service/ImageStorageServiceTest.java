package com.campus.secondhand.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

/**
 * 验证商品图片存储的路径、格式与大小限制。
 */
public class ImageStorageServiceTest {

	@Test
	public void storeGoodsImagesShouldReturnRuntimeRelativePathWhenUnderWorkingDirectory() throws Exception {
		// 默认图片目录应落在运行目录下，并返回可迁移的相对路径。
		String originalUserDir = System.getProperty("user.dir");
		Path tempDir = Files.createTempDirectory("java-xy-image-root");
		Path imagePath = tempDir.resolve("cover.jpg");
		Files.write(imagePath, "demo-image".getBytes(StandardCharsets.UTF_8));
		System.clearProperty("SECONDHAND_IMAGE_DIR");
		System.setProperty("user.dir", tempDir.toString());

		try {
			ImageStorageService service = new ImageStorageService();
			String storedPath = service.storeGoodsImages(42L, Collections.singletonList(imagePath)).get(0);
			String expectedRelativePath = Paths.get("data", "images", "goods-42", "01_cover.jpg").toString();
			Assert.assertEquals(expectedRelativePath, storedPath);
			Assert.assertTrue(Files.exists(tempDir.resolve(storedPath)));
		} finally {
			System.setProperty("user.dir", originalUserDir);
		}
	}

	@Test
	public void storeGoodsImagesShouldRejectUnsupportedFormat() throws Exception {
		// GIF 不在允许列表中，应被拒绝。
		Path tempDir = Files.createTempDirectory("java-xy-image-format");
		Path imagePath = tempDir.resolve("cover.gif");
		Files.write(imagePath, "gif".getBytes(StandardCharsets.UTF_8));

		try {
			new ImageStorageService().storeGoodsImages(1L, Collections.singletonList(imagePath));
			Assert.fail("GIF 图片不应通过");
		} catch (BusinessException expected) {
			Assert.assertEquals("仅支持 JPG、JPEG、PNG 格式图片", expected.getMessage());
		}
	}

	@Test
	public void storeGoodsImagesShouldRejectOversizedFile() throws Exception {
		// 超过 2MB 的文件应直接拒绝上传。
		Path tempDir = Files.createTempDirectory("java-xy-image-size");
		Path imagePath = tempDir.resolve("large.png");
		Files.write(imagePath, new byte[2 * 1024 * 1024 + 1]);

		try {
			new ImageStorageService().storeGoodsImages(1L, Collections.singletonList(imagePath));
			Assert.fail("超过 2MB 的图片不应通过");
		} catch (BusinessException expected) {
			Assert.assertEquals("单张图片大小不能超过 2MB", expected.getMessage());
		}
	}
}
