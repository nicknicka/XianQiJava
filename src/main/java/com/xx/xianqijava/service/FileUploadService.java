package com.xx.xianqijava.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传单个图片
     *
     * @param file 图片文件
     * @return 文件访问URL
     */
    String uploadImage(MultipartFile file);

    /**
     * 批量上传图片
     *
     * @param files 图片文件列表
     * @return 文件访问URL列表
     */
    String[] uploadImages(MultipartFile[] files);
}
