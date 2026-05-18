package com.campus.secondhand.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class ImagePreviewLoaderTest {

	@Test
	public void loadFromPathShouldReturnHideImageWhenPathIsBlank() {
		String imageSource = ImagePreviewLoader.resolveImageSource((String) null);

		Assert.assertEquals(ImagePreviewLoader.resolveHideImageSource(), imageSource);
	}

	@Test
	public void loadFromPathShouldReturnBrokenImageWhenFileDoesNotExist() {
		String imageSource = ImagePreviewLoader.resolveImageSource("/tmp/does-not-exist-preview.png");

		Assert.assertEquals(ImagePreviewLoader.resolveBrokenImageSource(), imageSource);
	}

	@Test
	public void loadFromPathShouldReturnBrokenImageWhenFileIsNotAValidImage() throws Exception {
		Path invalidFile = Files.createTempFile("java-xy-invalid-image", ".txt");
		Files.write(invalidFile, "not-an-image".getBytes(StandardCharsets.UTF_8));

		String imageSource = ImagePreviewLoader.resolveImageSource(invalidFile);

		Assert.assertEquals(ImagePreviewLoader.resolveBrokenImageSource(), imageSource);
	}

	@Test
	public void loadFromPathShouldReturnActualImageWhenFileIsValid() throws Exception {
		Path validFile = Files.createTempFile("java-xy-valid-image", ".png");
		Files.write(validFile, Files.readAllBytes(Paths.get("src/main/resources/images/hide_image.png")));

		String imageSource = ImagePreviewLoader.resolveImageSource(validFile);

		Assert.assertEquals(validFile.toFile().toURI().toString(), imageSource);
	}
}
