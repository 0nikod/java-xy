package com.campus.secondhand.service;

import com.campus.secondhand.config.AppConfig;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageStorageService {

	private static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024L * 1024L;
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

	public List<String> storeGoodsImages(Long goodsId, List<Path> sourcePaths) {
		// 图片按商品编号独立存放，便于删除商品时一次性清理目录。
		if (sourcePaths == null || sourcePaths.isEmpty()) {
			return new ArrayList<String>();
		}
		for (Path sourcePath : sourcePaths) {
			try {
				validateSourceImage(sourcePath);
			} catch (IOException e) {
				throw new IllegalStateException("保存商品图片失败", e);
			}
		}
		Path goodsDir = AppConfig.resolveImageStorageRoot().resolve("goods-" + goodsId);
		try {
			Files.createDirectories(goodsDir);
			clearDirectory(goodsDir);
			List<String> storedPaths = new ArrayList<String>();
			for (int i = 0; i < sourcePaths.size(); i++) {
				Path sourcePath = sourcePaths.get(i);
				String fileName = sourcePath.getFileName().toString();
				Path targetPath = goodsDir.resolve(String.format("%02d_%s", i + 1, fileName));
				Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				storedPaths.add(toStorablePath(targetPath));
			}
			return storedPaths;
		} catch (IOException e) {
			throw new IllegalStateException("保存商品图片失败", e);
		}
	}

	public void deleteGoodsImages(Long goodsId) {
		// 删除商品图片目录时，优先清空文件再删除目录，避免残留。
		Path goodsDir = AppConfig.resolveImageStorageRoot().resolve("goods-" + goodsId);
		if (!Files.exists(goodsDir)) {
			return;
		}
		try {
			clearDirectory(goodsDir);
			Files.deleteIfExists(goodsDir);
		} catch (IOException e) {
			throw new IllegalStateException("删除商品图片失败", e);
		}
	}

	private void validateSourceImage(Path sourcePath) throws IOException {
		// 图片规则在落盘前统一校验，保证控制器和测试环境都执行同一套约束。
		if (sourcePath == null || !Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
			throw new BusinessException("图片文件不存在");
		}
		String fileName = sourcePath.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		String extension = dotIndex >= 0 ? fileName.substring(dotIndex + 1).toLowerCase() : "";
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new BusinessException("仅支持 JPG、JPEG、PNG 格式图片");
		}
		if (Files.size(sourcePath) > MAX_IMAGE_SIZE_BYTES) {
			throw new BusinessException("单张图片大小不能超过 2MB");
		}
	}

	private void clearDirectory(Path directory) throws IOException {
		// 只清理当前目录内容，不递归处理子目录；当前项目图片目录结构保持扁平。
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path path : stream) {
				Files.deleteIfExists(path);
			}
		}
	}

	private String toStorablePath(Path targetPath) {
		Path normalizedTarget = targetPath.toAbsolutePath().normalize();
		Path workingDirectory = java.nio.file.Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
		if (normalizedTarget.startsWith(workingDirectory)) {
			return workingDirectory.relativize(normalizedTarget).toString();
		}
		return normalizedTarget.toString();
	}
}
