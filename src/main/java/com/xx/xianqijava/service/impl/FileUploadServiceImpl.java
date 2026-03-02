package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.FileUploadResultDTO;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.service.FileUploadService;
import com.xx.xianqijava.util.ImageUrlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类（本地存储）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Value("${file.upload.max-size:5242880}")
    private Long maxSize;

    private final ImageUrlBuilder imageUrlBuilder;

    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "webp");

    @Override
    public FileUploadResultDTO uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            // 生成UUID作为文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uuid = UUID.randomUUID().toString();
            String newFilename = uuid + "." + extension;

            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件（使用UUID.扩展名）
            Path filePath = Paths.get(uploadPath, newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 构建访问URL（使用伪地址）
            String fileUrl = imageUrlBuilder.buildImageUrl(uuid, extension);

            log.info("文件上传成功: uuid={}, extension={}, size={}", uuid, extension, file.getSize());

            return FileUploadResultDTO.builder()
                    .uuid(uuid)
                    .extension(extension)
                    .filename(newFilename)
                    .size(file.getSize())
                    .url(fileUrl)
                    .build();

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败");
        }
    }

    @Override
    public FileUploadResultDTO[] uploadImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的文件");
        }

        if (files.length > 9) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_EXCEED_LIMIT);
        }

        List<FileUploadResultDTO> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadImage(file));
        }

        return results.toArray(new FileUploadResultDTO[0]);
    }

    @Override
    public String uploadImageReturnUrl(MultipartFile file) {
        FileUploadResultDTO result = uploadImage(file);
        return result.getUrl();
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的文件");
        }

        // 校验文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED);
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
