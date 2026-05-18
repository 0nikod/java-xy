package com.campus.secondhand.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javafx.scene.image.Image;

/**
 * 统一处理预览图加载与占位图回退。
 */
public final class ImagePreviewLoader {

	private static final String HIDE_IMAGE_RESOURCE = "/images/hide_image.png";
	private static final String BROKEN_IMAGE_RESOURCE = "/images/broken_image.png";

	private ImagePreviewLoader() {
	}

	/**
	 * 按文件路径加载图片；空路径时返回“隐藏/无图”占位图，坏路径或坏文件时返回“损坏图片”占位图。
	 */
	public static Image loadFromPath(String imagePath) {
		return new Image(resolveImageSource(imagePath), false);
	}

	/**
	 * 按 Path 加载图片，供发布页本地预览复用。
	 */
	public static Image loadFromPath(Path imagePath) {
		return new Image(resolveImageSource(imagePath), false);
	}

	/**
	 * 无图片记录或未选择图片时的中性占位图。
	 */
	public static Image loadHideImage() {
		return new Image(resolveHideImageSource(), false);
	}

	/**
	 * 路径失效或解码失败时的错误占位图。
	 */
	public static Image loadBrokenImage() {
		return new Image(resolveBrokenImageSource(), false);
	}

	/**
	 * 解析字符串路径对应的预览图来源 URL。
	 */
	public static String resolveImageSource(String imagePath) {
		if (imagePath == null || imagePath.trim().isEmpty()) {
			return resolveHideImageSource();
		}
		return resolveExistingFile(new File(imagePath.trim()));
	}

	/**
	 * 解析 Path 对应的预览图来源 URL。
	 */
	public static String resolveImageSource(Path imagePath) {
		return imagePath == null ? resolveHideImageSource() : resolveExistingFile(imagePath.toFile());
	}

	public static String resolveHideImageSource() {
		return resolveResourceUrl(HIDE_IMAGE_RESOURCE);
	}

	public static String resolveBrokenImageSource() {
		return resolveResourceUrl(BROKEN_IMAGE_RESOURCE);
	}

	private static String resolveExistingFile(File file) {
		if (file == null || !file.exists() || !file.isFile()) {
			return resolveBrokenImageSource();
		}
		try {
			return ImageIO.read(file) == null ? resolveBrokenImageSource() : file.toURI().toString();
		} catch (IOException e) {
			return resolveBrokenImageSource();
		}
	}

	private static String resolveResourceUrl(String resourcePath) {
		java.net.URL resource = ImagePreviewLoader.class.getResource(resourcePath);
		if (resource == null) {
			throw new IllegalStateException("找不到图片资源: " + resourcePath);
		}
		return resource.toExternalForm();
	}
}
