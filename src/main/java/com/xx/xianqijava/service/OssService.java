package com.xx.xianqijava.service;

/**
 * 阿里云 OSS 服务接口
 */
public interface OssService {

    /**
     * 判断 OSS 是否启用
     */
    boolean isEnabled();

    /**
     * 获取图片 URL，支持指定尺寸
     *
     * @param filename 文件名
     * @param width    宽度（像素）
     * @param height   高度（像素，可选）
     * @return 图片 URL
     */
    String getImageUrl(String filename, Integer width, Integer height);

    /**
     * 获取缩略图 URL（200x200）
     *
     * @param filename 文件名
     * @return 缩略图 URL
     */
    String getThumbnailUrl(String filename);

    /**
     * 获取中等尺寸图 URL（800x800）
     *
     * @param filename 文件名
     * @return 中等尺寸图 URL
     */
    String getMediumUrl(String filename);

    /**
     * 获取原图 URL
     *
     * @param filename 文件名
     * @return 原图 URL
     */
    String getOriginalUrl(String filename);
}
