package com.xx.xianqijava.controller.admin;

import com.xx.xianqijava.annotation.OperationLog;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.FileUploadResultDTO;
import com.xx.xianqijava.service.FileUploadService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传控制器 - 管理端
 */
@Slf4j
@Tag(name = "文件上传管理", description = "管理员文件上传相关接口")
@RestController
@RequestMapping("/admin/upload")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-auth")
public class AdminFileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 上传单个图片
     */
    @Operation(summary = "上传单个图片", description = "管理员上传图片，支持轮播图、商品图片等")
    @PostMapping("/image")
    @OperationLog(module = "upload", action = "upload", description = "上传图片")
    public Result<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        Long adminId = SecurityUtil.getCurrentUserId();
        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();

        log.info("========== 管理员图片上传开始 ==========");
        log.info("管理员ID: {}, 原始文件名: {}, 文件大小: {} bytes ({} KB)",
            adminId, originalFilename, fileSize, String.format("%.2f", fileSize / 1024.0));
        log.info("文件ContentType: {}", file.getContentType());

        try {
            FileUploadResultDTO uploadResult = fileUploadService.uploadImage(file);

            FileUploadVO uploadVO = new FileUploadVO();
            uploadVO.setUuid(uploadResult.getUuid());
            uploadVO.setUrl(uploadResult.getUrl());
            uploadVO.setFilename(uploadResult.getFilename());
            uploadVO.setOriginalFilename(originalFilename);
            uploadVO.setSize(uploadResult.getSize());
            uploadVO.setExtension(uploadResult.getExtension());

            log.info("管理员上传图片成功 - 管理员ID: {}, UUID: {}, 访问URL: {}, 文件大小: {} bytes",
                adminId, uploadVO.getUuid(), uploadVO.getUrl(), uploadVO.getSize());
            log.info("========== 管理员图片上传完成 ==========");

            return Result.success("上传成功", uploadVO);
        } catch (Exception e) {
            log.error("========== 管理员图片上传失败 ==========");
            log.error("管理员ID: {}, 文件名: {}, 错误信息: {}", adminId, originalFilename, e.getMessage(), e);
            log.error("====================================");
            throw e;
        }
    }

    /**
     * 批量上传图片
     */
    @Operation(summary = "批量上传图片", description = "管理员批量上传图片")
    @PostMapping("/images")
    @OperationLog(module = "upload", action = "batch_upload", description = "批量上传图片")
    public Result<List<FileUploadVO>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        Long adminId = SecurityUtil.getCurrentUserId();

        log.info("========== 管理员批量图片上传开始 ==========");
        log.info("管理员ID: {}, 请求数量: {}", adminId, files.length);

        try {
            FileUploadResultDTO[] uploadResults = fileUploadService.uploadImages(files);

            List<FileUploadVO> uploadVOs = new ArrayList<>();
            for (int i = 0; i < uploadResults.length; i++) {
                FileUploadResultDTO uploadResult = uploadResults[i];
                FileUploadVO uploadVO = new FileUploadVO();
                uploadVO.setUuid(uploadResult.getUuid());
                uploadVO.setUrl(uploadResult.getUrl());
                uploadVO.setFilename(uploadResult.getFilename());
                uploadVO.setOriginalFilename(files[i].getOriginalFilename());
                uploadVO.setSize(uploadResult.getSize());
                uploadVO.setExtension(uploadResult.getExtension());
                uploadVOs.add(uploadVO);

                log.info("文件 [{}/{}] 上传成功 - 原文件名: {}, UUID: {}, URL: {}",
                    i + 1, files.length, uploadVO.getOriginalFilename(),
                    uploadVO.getUuid(), uploadVO.getUrl());
            }

            log.info("管理员批量图片上传全部完成 - 管理员ID: {}, 成功: {}/{}", adminId, uploadVOs.size(), files.length);
            log.info("========================================");

            return Result.success("上传成功", uploadVOs);
        } catch (Exception e) {
            log.error("========== 管理员批量图片上传失败 ==========");
            log.error("管理员ID: {}, 请求数量: {}, 错误信息: {}", adminId, files.length, e.getMessage(), e);
            log.error("=========================================");
            throw e;
        }
    }
}
