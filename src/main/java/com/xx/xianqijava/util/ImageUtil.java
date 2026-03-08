package com.xx.xianqijava.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 图片处理工具类
 * 提供图片缩放、裁剪等功能
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
public class ImageUtil {

    /**
     * 图片尺寸枚举
     */
    public enum ImageSize {
        /**
         * 缩略图 200x200
         */
        THUMBNAIL(200, 200, "_thumbnail"),

        /**
         * 中等尺寸图 800x800
         */
        MEDIUM(800, 800, "_medium"),

        /**
         * 原图
         */
        ORIGINAL(0, 0, "");

        private final int width;
        private final int height;
        private final String suffix;

        ImageSize(int width, int height, String suffix) {
            this.width = width;
            this.height = height;
            this.suffix = suffix;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getSuffix() {
            return suffix;
        }
    }

    /**
     * 缩放图片并保存到目标路径
     *
     * @param sourcePath 源图片路径
     * @param targetPath 目标图片路径
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 是否成功
     */
    public static boolean resizeImage(Path sourcePath, Path targetPath, int targetWidth, int targetHeight) {
        try {
            // 读取源图片
            BufferedImage sourceImage = ImageIO.read(sourcePath.toFile());
            if (sourceImage == null) {
                log.error("无法读取图片: {}", sourcePath);
                return false;
            }

            // 计算缩放后的尺寸
            int[] dimensions = calculateDimensions(sourceImage.getWidth(), sourceImage.getHeight(), targetWidth, targetHeight);
            int scaledWidth = dimensions[0];
            int scaledHeight = dimensions[1];

            // 创建缩放后的图片
            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = scaledImage.createGraphics();

            // 设置渲染质量
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制缩放后的图片
            graphics2D.drawImage(sourceImage, 0, 0, scaledWidth, scaledHeight, null);
            graphics2D.dispose();

            // 确保目标目录存在
            Path parentDir = targetPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 获取文件格式
            String formatName = getImageFormatName(sourcePath.getFileName().toString());

            // 保存图片
            boolean success = ImageIO.write(scaledImage, formatName, targetPath.toFile());

            if (success) {
                log.debug("图片缩放成功 - 源文件: {}, 目标文件: {}, 尺寸: {}x{}",
                    sourcePath.getFileName(), targetPath.getFileName(), scaledWidth, scaledHeight);
            } else {
                log.error("图片保存失败: {}", targetPath);
            }

            return success;

        } catch (IOException e) {
            log.error("图片缩放失败 - 源文件: {}, 目标文件: {}, 错误: {}",
                sourcePath, targetPath, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取或生成指定尺寸的图片
     * 如果缓存中存在，直接返回；否则生成并保存到缓存
     *
     * @param sourcePath 源图片路径
     * @param cacheDir 缓存目录
     * @param imageSize 图片尺寸
     * @return 处理后的图片路径
     */
    public static Path getOrGenerateSizedImage(Path sourcePath, Path cacheDir, ImageSize imageSize) {
        if (imageSize == ImageSize.ORIGINAL) {
            return sourcePath;
        }

        String filename = sourcePath.getFileName().toString();
        String baseName = removeExtension(filename);
        String extension = getExtension(filename);
        String cacheFileName = baseName + imageSize.getSuffix() + "." + extension;
        Path cachePath = Paths.get(cacheDir.toString(), cacheFileName);

        // 如果缓存文件存在，直接返回
        if (Files.exists(cachePath)) {
            log.debug("使用缓存图片: {}", cachePath.getFileName());
            return cachePath;
        }

        // 生成缩放图片
        boolean success = resizeImage(sourcePath, cachePath, imageSize.getWidth(), imageSize.getHeight());
        if (success) {
            return cachePath;
        } else {
            // 如果生成失败，返回原图
            log.warn("生成缩放图片失败，返回原图: {}", sourcePath);
            return sourcePath;
        }
    }

    /**
     * 计算缩放后的尺寸，保持宽高比
     *
     * @param sourceWidth 源宽度
     * @param sourceHeight 源高度
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return [缩放后宽度, 缩放后高度]
     */
    private static int[] calculateDimensions(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        if (targetWidth == 0 || targetHeight == 0) {
            return new int[]{sourceWidth, sourceHeight};
        }

        double sourceRatio = (double) sourceWidth / sourceHeight;
        double targetRatio = (double) targetWidth / targetHeight;

        int scaledWidth, scaledHeight;

        if (sourceRatio > targetRatio) {
            // 源图片更宽，以宽度为准
            scaledWidth = targetWidth;
            scaledHeight = (int) (targetWidth / sourceRatio);
        } else {
            // 源图片更高，以高度为准
            scaledHeight = targetHeight;
            scaledWidth = (int) (targetHeight * sourceRatio);
        }

        // 确保不小于1像素
        scaledWidth = Math.max(1, scaledWidth);
        scaledHeight = Math.max(1, scaledHeight);

        return new int[]{scaledWidth, scaledHeight};
    }

    /**
     * 获取图片格式名称
     */
    private static String getImageFormatName(String filename) {
        String extension = getExtension(filename);
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "jpg";
            case "png" -> "png";
            case "gif" -> "gif";
            case "bmp" -> "bmp";
            default -> "jpg";
        };
    }

    /**
     * 获取文件扩展名
     */
    private static String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * 移除文件扩展名
     */
    private static String removeExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return filename;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }
        return filename;
    }

    /**
     * 获取图片尺寸
     *
     * @param imagePath 图片路径
     * @return [宽度, 高度]，如果读取失败返回 null
     */
    public static int[] getImageSize(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                return null;
            }
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (IOException e) {
            log.error("读取图片尺寸失败: {}", imagePath, e);
            return null;
        }
    }

    /**
     * 从 InputStream 读取图片并缩放后写入 OutputStream
     *
     * @param inputStream 输入流
     * @param outputStream 输出流
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @param formatName 图片格式（jpg、png等）
     * @return 是否成功
     */
    public static boolean resizeImageFromStream(
            InputStream inputStream,
            OutputStream outputStream,
            int targetWidth,
            int targetHeight,
            String formatName) {

        try {
            // 读取源图片
            BufferedImage sourceImage = ImageIO.read(inputStream);
            if (sourceImage == null) {
                log.error("无法从输入流读取图片");
                return false;
            }

            // 计算缩放后的尺寸
            int[] dimensions = calculateDimensions(sourceImage.getWidth(), sourceImage.getHeight(), targetWidth, targetHeight);
            int scaledWidth = dimensions[0];
            int scaledHeight = dimensions[1];

            // 创建缩放后的图片
            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = scaledImage.createGraphics();

            // 设置渲染质量
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制缩放后的图片
            graphics2D.drawImage(sourceImage, 0, 0, scaledWidth, scaledHeight, null);
            graphics2D.dispose();

            // 保存到输出流
            return ImageIO.write(scaledImage, formatName, outputStream);

        } catch (IOException e) {
            log.error("图片流处理失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
