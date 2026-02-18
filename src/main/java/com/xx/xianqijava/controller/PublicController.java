package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公共接口控制器
 */
@Tag(name = "公共接口")
@RestController
@RequestMapping("/public")
public class PublicController {

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("系统运行正常");
    }

    /**
     * 获取系统配置
     */
    @Operation(summary = "获取公开的系统配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("app_name", "校园易购");
        config.put("version", "1.0.0");
        config.put("upload_max_size", 5242880);
        config.put("upload_allowed_types", new String[]{"jpg", "jpeg", "png", "webp"});
        config.put("product_max_images", 9);
        return Result.success(config);
    }
}
