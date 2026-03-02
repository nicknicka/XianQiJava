package com.xx.xianqijava.service;

import com.xx.xianqijava.dto.FileUploadResultDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传单个图片
     *
     * @param file 图片文件
     * @return 文件上传结果（包含UUID等信息）
     */
    FileUploadResultDTO uploadImage(MultipartFile file);

    /**
     * 批量上传图片
     *
     * @param files 图片文件列表
     * @return 文件上传结果列表
     */
    FileUploadResultDTO[] uploadImages(MultipartFile[] files);

    /**
     * 上传单个图片（兼容旧接口，返回完整URL）
     *
     * @param file 图片文件
     * @return 文件访问URL
     * @deprecated 使用 uploadImage() 返回更详细的信息
     */
    @Deprecated
    String uploadImageReturnUrl(MultipartFile file);
}
