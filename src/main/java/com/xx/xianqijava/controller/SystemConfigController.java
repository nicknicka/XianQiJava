package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.SystemConfigCreateDTO;
import com.xx.xianqijava.entity.SystemConfig;
import com.xx.xianqijava.service.SystemConfigService;
import com.xx.xianqijava.vo.SystemConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器
 */
@Slf4j
@Tag(name = "系统配置管理")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 创建系统配置
     */
    @PostMapping
    @Operation(summary = "创建系统配置")
    public Result<SystemConfigVO> createConfig(@Valid @RequestBody SystemConfigCreateDTO createDTO) {
        log.info("创建系统配置, configKey={}", createDTO.getConfigKey());
        SystemConfigVO configVO = systemConfigService.createConfig(createDTO);
        return Result.success("配置创建成功", configVO);
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/{configId}")
    @Operation(summary = "更新系统配置")
    public Result<SystemConfigVO> updateConfig(
            @Parameter(description = "配置ID") @PathVariable("configId") Long configId,
            @Valid @RequestBody SystemConfigCreateDTO createDTO) {
        log.info("更新系统配置, configId={}", configId);
        SystemConfigVO configVO = systemConfigService.updateConfig(configId, createDTO);
        return Result.success("配置更新成功", configVO);
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/{configId}")
    @Operation(summary = "删除系统配置")
    public Result<Void> deleteConfig(
            @Parameter(description = "配置ID") @PathVariable("configId") Long configId) {
        log.info("删除系统配置, configId={}", configId);
        systemConfigService.deleteConfig(configId);
        return Result.success("配置删除成功");
    }

    /**
     * 获取配置列表（分页）
     */
    @GetMapping
    @Operation(summary = "获取配置列表")
    public Result<IPage<SystemConfigVO>> getConfigList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "分组名称") @RequestParam(required = false) String groupName) {
        log.info("查询系统配置列表, page={}, size={}, groupName={}", page, size, groupName);

        Page<SystemConfig> pageParam = new Page<>(page, size);
        IPage<SystemConfigVO> configPage = systemConfigService.getConfigList(pageParam, groupName);

        return Result.success(configPage);
    }

    /**
     * 获取配置值
     */
    @GetMapping("/value/{configKey}")
    @Operation(summary = "获取配置值")
    public Result<String> getConfigValue(
            @Parameter(description = "配置键") @PathVariable("configKey") String configKey) {
        String value = systemConfigService.getConfigValue(configKey);
        return Result.success(value);
    }

    /**
     * 获取公开配置（Map形式）
     */
    @GetMapping("/public")
    @Operation(summary = "获取公开配置")
    public Result<Map<String, Object>> getPublicConfigs() {
        log.info("获取公开配置列表");
        Map<String, Object> configs = systemConfigService.getPublicConfigs();
        return Result.success(configs);
    }

    /**
     * 根据分组获取配置
     */
    @GetMapping("/group/{groupName}")
    @Operation(summary = "根据分组获取配置")
    public Result<Map<String, String>> getConfigsByGroup(
            @Parameter(description = "分组名称") @PathVariable("groupName") String groupName) {
        log.info("获取分组配置, groupName={}", groupName);
        Map<String, String> configs = systemConfigService.getConfigsByGroup(groupName);
        return Result.success(configs);
    }
}
