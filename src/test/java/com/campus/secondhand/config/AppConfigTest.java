package com.campus.secondhand.config;

import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

public class AppConfigTest {

	@Test
	public void resolveImageStorageRootShouldRespectConfiguredDirectory() {
		System.setProperty("SECONDHAND_IMAGE_DIR", "build/test-images");
		try {
			Path root = AppConfig.resolveImageStorageRoot();
			Assert.assertTrue(root.toString().endsWith("build/test-images"));
		} finally {
			System.clearProperty("SECONDHAND_IMAGE_DIR");
		}
	}
}
