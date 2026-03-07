package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.ProductImage;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ProductImageMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片访问控制器
 * 提供图片的安全访问接口，隐藏真实文件路径
 */
@Slf4j
@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
@Tag(name = "图片访问", description = "图片访问相关接口")
public class ImageController {

    private final ProductImageMapper productImageMapper;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    /**
     * 通过UUID直接访问文件（不检查数据库）
     * 用于反馈图片、头像等不需要数据库记录的文件
     * 前端请求: GET /api/image/file/{uuid}.jpg
     */
    @GetMapping("/file/{filename}**")
    @Operation(summary = "直接获取文件", description = "通过UUID直接获取文件，不检查数据库记录")
    public ResponseEntity<Resource> getFileDirect(
            @Parameter(description = "文件名（UUID.扩展名）") @PathVariable String filename) {

        log.debug("========== 图片访问请求（直接模式）==========");
        log.debug("请求文件名: {}", filename);

        try {
            // 解析UUID和扩展名
            String uuid = extractUuid(filename);
            String extension = extractExtension(filename);

            if (!StringUtils.hasText(uuid) || !StringUtils.hasText(extension)) {
                log.warn("✗ 无效的文件名格式: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            log.debug("解析结果 - UUID: {}, 扩展名: {}", uuid, extension);

            // 构建文件路径
            String actualFilename = uuid + "." + extension;
            Path filePath = Paths.get(uploadPath, actualFilename);

            if (!Files.exists(filePath)) {
                log.warn("✗ 文件不存在: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // 读取文件
            Resource resource = new FileSystemResource(filePath);
            long fileSize = resource.contentLength();

            // 根据扩展名确定Content-Type
            MediaType mediaType = getMediaType(extension);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(fileSize);

            // 记录访问日志
            log.info("✓ 文件访问成功（直接模式）- 文件名: {}, 大小: {} bytes ({} KB), 类型: {}",
                actualFilename, fileSize, String.format("%.2f", fileSize / 1024.0), mediaType);
            log.debug("============================================");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("✗ 获取文件失败 - 文件名: {}, 错误: {}", filename, e.getMessage(), e);
            log.debug("============================================");
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 通过UUID访问图片
     * 前端请求: GET /api/image/{uuid}.jpg
     * 后端验证权限后返回真实图片数据
     *
     * 优先从数据库查询，如果找不到则直接从文件系统返回
     */
    @GetMapping("/{filename}**")
    @Operation(summary = "获取图片", description = "通过UUID获取图片，隐藏真实路径")
    public ResponseEntity<Resource> getImage(
            @Parameter(description = "文件名（UUID.扩展名）") @PathVariable String filename) {

        log.debug("========== 图片访问请求（数据库模式）==========");
        log.debug("请求文件名: {}", filename);

        try {
            // 解析UUID和扩展名
            String uuid = extractUuid(filename);
            String extension = extractExtension(filename);

            if (!StringUtils.hasText(uuid) || !StringUtils.hasText(extension)) {
                log.warn("✗ 无效的文件名格式: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            log.debug("解析结果 - UUID: {}, 扩展名: {}", uuid, extension);

            // 构建文件路径
            String actualFilename = uuid + "." + extension;
            Path filePath = Paths.get(uploadPath, actualFilename);

            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                log.warn("✗ 文件不存在: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // 尝试从数据库查询（用于商品图片）
            ProductImage imageInfo = productImageMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getImageUuid, uuid)
                            .eq(ProductImage::getStatus, 0)
            );

            // 如果数据库中不存在，直接返回文件（用于反馈图片、头像等）
            if (imageInfo == null) {
                log.debug("数据库中无记录，直接返回文件: uuid={}", uuid);
            } else {
                log.debug("数据库记录存在 - 商品ID: {}, 图片状态: {}", imageInfo.getProductId(), imageInfo.getStatus());
            }

            // 读取文件
            Resource resource = new FileSystemResource(filePath);
            long fileSize = resource.contentLength();

            // 根据扩展名确定Content-Type
            MediaType mediaType = getMediaType(extension);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(fileSize);

            // 记录访问日志
            if (imageInfo != null) {
                log.info("✓ 图片访问成功（数据库模式）- UUID: {}, 商品ID: {}, 大小: {} bytes ({} KB)",
                    uuid, imageInfo.getProductId(), fileSize, String.format("%.2f", fileSize / 1024.0));
            } else {
                log.info("✓ 图片访问成功（无数据库记录）- UUID: {}, 大小: {} bytes ({} KB)",
                    uuid, fileSize, String.format("%.2f", fileSize / 1024.0));
            }
            log.debug("==============================================");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("✗ 获取图片失败 - 文件名: {}, 错误: {}", filename, e.getMessage(), e);
            log.debug("==============================================");
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取缩略图
     */
    @GetMapping("/thumbnail/{filename}**")
    @Operation(summary = "获取缩略图", description = "获取图片的缩略图版本")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String filename) {
        // TODO: 实现缩略图逻辑
        // 目前暂时返回原图，后续可以集成图片处理库生成缩略图
        return getImage(filename);
    }

    /**
     * 获取中等尺寸图片
     */
    @GetMapping("/medium/{filename}**")
    @Operation(summary = "获取中等尺寸图片", description = "获取图片的中等尺寸版本")
    public ResponseEntity<Resource> getMedium(@PathVariable String filename) {
        // TODO: 实现中等尺寸图逻辑
        // 目前暂时返回原图，后续可以集成图片处理库生成中等尺寸图
        return getImage(filename);
    }

    /**
     * 检查图片是否存在
     */
    @GetMapping("/check/{uuid}")
    @Operation(summary = "检查图片是否存在", description = "验证UUID对应的图片是否存在")
    public ResponseEntity<Map<String, Object>> checkImage(
            @Parameter(description = "图片UUID") @PathVariable String uuid) {

        Map<String, Object> response = new HashMap<>();

        try {
            ProductImage imageInfo = productImageMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getImageUuid, uuid)
                            .eq(ProductImage::getStatus, 0)
            );

            if (imageInfo == null) {
                response.put("exists", false);
                response.put("message", "图片不存在");
                return ResponseEntity.ok(response);
            }

            // 检查文件是否存在
            String extension = imageInfo.getFileExtension();
            Path filePath = Paths.get(uploadPath, uuid + "." + extension);
            boolean fileExists = Files.exists(filePath);

            response.put("exists", fileExists);
            response.put("uuid", uuid);
            response.put("productId", imageInfo.getProductId());

            if (fileExists) {
                response.put("size", Files.size(filePath));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("检查图片失败: uuid={}", uuid, e);
            response.put("exists", false);
            response.put("error", "检查失败");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 从文件名中提取UUID
     */
    private String extractUuid(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }
        return filename;
    }

    /**
     * 从文件名中提取扩展名
     */
    private String extractExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return null;
    }

    /**
     * 根据扩展名获取MediaType
     */
    private MediaType getMediaType(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.valueOf("image/webp");
            case "svg" -> MediaType.valueOf("image/svg+xml");
            case "bmp" -> MediaType.valueOf("image/bmp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
