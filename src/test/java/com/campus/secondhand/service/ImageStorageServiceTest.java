package com.campus.secondhand.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ImageStorageServiceTest {

	@Test
	public void storeGoodsImagesShouldRespectConfiguredDirectory() throws Exception {
		Path tempDir = Files.createTempDirectory("java-xy-image-root");
		Path imagePath = tempDir.resolve("cover.jpg");
		Files.write(imagePath, "demo-image".getBytes(StandardCharsets.UTF_8));
		System.setProperty("SECONDHAND_IMAGE_DIR", tempDir.resolve("storage").toString());

		try {
			ImageStorageService service = new ImageStorageService();
			String storedPath = service.storeGoodsImages(42L, Collections.singletonList(imagePath)).get(0);
			Assert.assertTrue(storedPath.contains("goods-42"));
			Assert.assertTrue(Files.exists(Paths.get(storedPath)));
		} finally {
			System.clearProperty("SECONDHAND_IMAGE_DIR");
		}
	}

	@Test
	public void storeGoodsImagesShouldRejectUnsupportedFormat() throws Exception {
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
