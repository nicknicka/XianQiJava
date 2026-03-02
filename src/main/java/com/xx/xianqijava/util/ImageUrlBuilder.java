package com.xx.xianqijava.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 图片URL构建工具类
 * 负责将UUID转换为可访问的完整URL
 */
@Slf4j
@Component
public class ImageUrlBuilder {

    /**
     * 应用基础URL（如：https://api.example.com）
     */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 图片访问路径前缀（伪地址）
     */
    @Value("${app.image-url-prefix:/api/image}")
    private String imageUrlPrefix;

    /**
     * 文件上传的URL前缀（用于静态资源）
     */
    @Value("${file.upload.url-prefix:/uploads}")
    private String uploadUrlPrefix;

    /**
     * 构建图片访问URL（使用伪地址接口）
     * 前端访问: /api/image/{uuid}
     * 后端读取真实文件并返回
     *
     * @param uuid         图片UUID
     * @param extension    文件扩展名
     * @return 完整的图片访问URL
     */
    public String buildImageUrl(String uuid, String extension) {
        if (uuid == null || extension == null) {
            return null;
        }
        return baseUrl + imageUrlPrefix + "/" + uuid + "." + extension;
    }

    /**
     * 构建缩略图URL
     *
     * @param uuid      图片UUID
     * @param extension 文件扩展名
     * @return 缩略图URL
     */
    public String buildThumbnailUrl(String uuid, String extension) {
        if (uuid == null || extension == null) {
            return null;
        }
        return baseUrl + imageUrlPrefix + "/thumbnail/" + uuid + "." + extension;
    }

    /**
     * 构建中等尺寸图片URL
     *
     * @param uuid      图片UUID
     * @param extension 文件扩展名
     * @return 中等尺寸图片URL
     */
    public String buildMediumUrl(String uuid, String extension) {
        if (uuid == null || extension == null) {
            return null;
        }
        return baseUrl + imageUrlPrefix + "/medium/" + uuid + "." + extension;
    }

    /**
     * 从URL中提取UUID
     * 例如: /api/image/abc123-def456.jpg -> abc123-def456
     *
     * @param url 图片URL
     * @return UUID
     */
    public String extractUuid(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // 去除基础URL
        String path = url.replace(baseUrl, "");

        // 去除前缀
        if (path.startsWith(imageUrlPrefix)) {
            path = path.substring(imageUrlPrefix.length());
        }

        // 提取文件名（去掉扩展名）
        String filename = path.replaceAll("^/", "").split("/")[0];
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }

        return filename;
    }

    /**
     * 从URL中提取扩展名
     *
     * @param url 图片URL
     * @return 扩展名（如: jpg, png）
     */
    public String extractExtension(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        int lastDotIndex = url.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < url.length() - 1) {
            return url.substring(lastDotIndex + 1).toLowerCase();
        }

        return null;
    }

    /**
     * 获取当前配置的基础URL
     *
     * @return 基础URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 获取图片URL前缀
     *
     * @return 图片URL前缀
     */
    public String getImageUrlPrefix() {
        return imageUrlPrefix;
    }
}
