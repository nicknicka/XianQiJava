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
        // 校验文件
        log.debug("开始校验文件 - 原始文件名: {}, 大小: {} bytes",
            file.getOriginalFilename(), file.getSize());
        validateFile(file);
        log.debug("文件校验通过");

        try {
            // 生成UUID作为文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uuid = UUID.randomUUID().toString();
            String newFilename = uuid + "." + extension;

            log.debug("生成文件信息 - UUID: {}, 扩展名: {}, 新文件名: {}", uuid, extension, newFilename);

            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                log.info("上传目录不存在，创建目录: {}", uploadPath);
                boolean created = uploadDir.mkdirs();
                log.info("目录创建结果: {}", created ? "成功" : "失败");
            }

            // 保存文件（使用UUID.扩展名）
            Path filePath = Paths.get(uploadPath, newFilename);
            log.debug("准备保存文件 - 目标路径: {}", filePath.toAbsolutePath());

            long startTime = System.currentTimeMillis();
            Files.copy(file.getInputStream(), filePath);
            long endTime = System.currentTimeMillis();

            log.debug("文件保存成功 - 耗时: {} ms", endTime - startTime);

            // 构建访问URL（使用伪地址）
            String fileUrl = imageUrlBuilder.buildImageUrl(uuid, extension);
            log.debug("生成访问URL: {}", fileUrl);

            log.info("✓ 文件上传成功 - UUID: {}, 扩展名: {}, 大小: {} bytes, URL: {}",
                uuid, extension, file.getSize(), fileUrl);

            return FileUploadResultDTO.builder()
                    .uuid(uuid)
                    .extension(extension)
                    .filename(newFilename)
                    .size(file.getSize())
                    .url(fileUrl)
                    .build();

        } catch (IOException e) {
            log.error("✗ 文件上传失败 - 原始文件名: {}, 文件大小: {} bytes, 错误: {}",
                file.getOriginalFilename(), file.getSize(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResultDTO[] uploadImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            log.warn("✗ 批量上传校验失败: 文件数组为空");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的文件");
        }

        log.debug("批量上传文件数量校验 - 当前数量: {}, 最大限制: 9", files.length);
        if (files.length > 9) {
            log.warn("✗ 批量上传数量超限 - 当前数量: {}, 最大限制: 9", files.length);
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_EXCEED_LIMIT);
        }

        log.debug("✓ 批量上传文件数量校验通过 - 数量: {}", files.length);

        List<FileUploadResultDTO> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            log.debug("========== 处理第 {} 个文件 ==========", i + 1);
            try {
                FileUploadResultDTO result = uploadImage(file);
                results.add(result);
                successCount++;
                log.debug("第 {} 个文件处理成功", i + 1);
            } catch (Exception e) {
                failCount++;
                log.error("第 {} 个文件处理失败 - 文件名: {}", i + 1, file.getOriginalFilename(), e);
                // 继续处理其他文件，不中断整个批量上传
            }
            log.debug("======================================");
        }

        log.info("批量上传处理完成 - 总数: {}, 成功: {}, 失败: {}",
            files.length, successCount, failCount);

        if (failCount > 0) {
            log.warn("警告: 有 {} 个文件上传失败", failCount);
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
            log.warn("✗ 文件校验失败: 文件为空");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的文件");
        }

        // 校验文件大小
        long fileSize = file.getSize();
        log.debug("校验文件大小 - 当前大小: {} bytes, 最大限制: {} bytes", fileSize, maxSize);
        if (fileSize > maxSize) {
            log.warn("✗ 文件大小超限 - 当前: {} bytes ({} MB), 限制: {} bytes ({} MB)",
                fileSize, String.format("%.2f", fileSize / 1024.0 / 1024.0),
                maxSize, String.format("%.2f", maxSize / 1024.0 / 1024.0));
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED);
        }
        log.debug("✓ 文件大小校验通过");

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        log.debug("校验文件类型 - 扩展名: {}, 允许的类型: {}", extension, ALLOWED_IMAGE_TYPES);

        if (!ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase())) {
            log.warn("✗ 文件类型不支持 - 扩展名: {}, 允许的类型: {}", extension, ALLOWED_IMAGE_TYPES);
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }
        log.debug("✓ 文件类型校验通过 - 扩展名: {}", extension);
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
