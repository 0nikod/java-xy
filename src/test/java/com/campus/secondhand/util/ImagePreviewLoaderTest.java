package com.campus.secondhand.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

/**
 * 验证图片预览加载器在不同输入下的回退行为。
 */
public class ImagePreviewLoaderTest {

	@Test
	public void loadFromPathShouldReturnHideImageWhenPathIsBlank() {
		// 空路径应返回默认隐藏图，避免界面出现空白。
		String imageSource = ImagePreviewLoader.resolveImageSource((String) null);

		Assert.assertEquals(ImagePreviewLoader.resolveHideImageSource(), imageSource);
	}

	@Test
	public void loadFromPathShouldReturnBrokenImageWhenFileDoesNotExist() {
		// 文件不存在时应返回破损图片占位。
		String imageSource = ImagePreviewLoader.resolveImageSource("/tmp/does-not-exist-preview.png");

		Assert.assertEquals(ImagePreviewLoader.resolveBrokenImageSource(), imageSource);
	}

	@Test
	public void loadFromPathShouldReturnBrokenImageWhenFileIsNotAValidImage() throws Exception {
		// 非法内容即使扩展名像图片，也应识别为破损资源。
		Path invalidFile = Files.createTempFile("java-xy-invalid-image", ".txt");
		Files.write(invalidFile, "not-an-image".getBytes(StandardCharsets.UTF_8));

		String imageSource = ImagePreviewLoader.resolveImageSource(invalidFile);

		Assert.assertEquals(ImagePreviewLoader.resolveBrokenImageSource(), imageSource);
	}

	@Test
	public void loadFromPathShouldReturnActualImageWhenFileIsValid() throws Exception {
		// 有效图片应返回文件真实地址，用于正常预览。
		Path validFile = Files.createTempFile("java-xy-valid-image", ".png");
		Files.write(validFile, Files.readAllBytes(Paths.get("src/main/resources/images/hide_image.png")));

		String imageSource = ImagePreviewLoader.resolveImageSource(validFile);

		Assert.assertEquals(validFile.toFile().toURI().toString(), imageSource);
	}
}
