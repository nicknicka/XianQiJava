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
        log.info("用户 {} 上传图片: {}", userId, file.getOriginalFilename());

        FileUploadResultDTO uploadResult = fileUploadService.uploadImage(file);

        FileUploadVO uploadVO = new FileUploadVO();
        uploadVO.setUuid(uploadResult.getUuid());
        uploadVO.setUrl(uploadResult.getUrl());
        uploadVO.setFilename(uploadResult.getFilename());
        uploadVO.setOriginalFilename(file.getOriginalFilename());
        uploadVO.setSize(uploadResult.getSize());
        uploadVO.setExtension(uploadResult.getExtension());

        return Result.success("上传成功", uploadVO);
    }

    /**
     * 批量上传图片
     */
    @Operation(summary = "批量上传图片")
    @PostMapping("/images")
    public Result<List<FileUploadVO>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("用户 {} 批量上传图片，数量: {}", userId, files.length);

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
        }

        return Result.success("上传成功", uploadVOs);
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
