package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.FileUploadResultDTO;
import com.xx.xianqijava.service.FileUploadService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 上传单个图片
     */
    @Operation(summary = "上传单个图片")
    @PostMapping("/image")
    public Result<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        String originalFilename = file.getOriginalFilename();
        long fileSize = file.getSize();

        log.info("========== 图片上传开始 ==========");
        log.info("用户ID: {}, 原始文件名: {}, 文件大小: {} bytes ({} KB)",
            userId, originalFilename, fileSize, String.format("%.2f", fileSize / 1024.0));
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

            log.info("图片上传成功 - UUID: {}, 访问URL: {}, 文件大小: {} bytes",
                uploadVO.getUuid(), uploadVO.getUrl(), uploadVO.getSize());
            log.info("========== 图片上传完成 ==========");

            return Result.success("上传成功", uploadVO);
        } catch (Exception e) {
            log.error("========== 图片上传失败 ==========");
            log.error("用户ID: {}, 文件名: {}, 错误信息: {}", userId, originalFilename, e.getMessage(), e);
            log.error("====================================");
            throw e;
        }
    }

    /**
     * 批量上传图片
     */
    @Operation(summary = "批量上传图片")
    @PostMapping("/images")
    public Result<List<FileUploadVO>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();

        log.info("========== 批量图片上传开始 ==========");
        log.info("用户ID: {}, 请求数量: {}", userId, files.length);

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

            log.info("批量图片上传全部完成 - 成功: {}/{}", uploadVOs.size(), files.length);
            log.info("========================================");

            return Result.success("上传成功", uploadVOs);
        } catch (Exception e) {
            log.error("========== 批量图片上传失败 ==========");
            log.error("用户ID: {}, 请求数量: {}, 错误信息: {}", userId, files.length, e.getMessage(), e);
            log.error("=========================================");
            throw e;
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
