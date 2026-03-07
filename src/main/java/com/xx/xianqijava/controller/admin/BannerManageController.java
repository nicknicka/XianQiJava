package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.BannerCreateDTO;
import com.xx.xianqijava.dto.admin.BannerManageQueryDTO;
import com.xx.xianqijava.dto.admin.BannerUpdateDTO;
import com.xx.xianqijava.service.BannerManageService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.admin.BannerManageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图管理控制器 - 管理端
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/banner")
@Tag(name = "轮播图管理", description = "轮播图管理相关接口")
@SecurityRequirement(name = "bearer-auth")
public class BannerManageController {

    private final BannerManageService bannerManageService;

    /**
     * 分页查询轮播图列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询轮播图列表", description = "支持多种条件筛选和排序")
    public Page<BannerManageVO> getBannerList(BannerManageQueryDTO queryDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}查询轮播图列表，查询条件：{}", adminId, queryDTO);
        return bannerManageService.getBannerList(queryDTO);
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/{bannerId}")
    @Operation(summary = "获取轮播图详情", description = "根据轮播图ID获取详细信息")
    public BannerManageVO getBannerDetail(@PathVariable Long bannerId) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取轮播图详情，轮播图ID：{}", adminId, bannerId);
        return bannerManageService.getBannerDetail(bannerId);
    }

    /**
     * 创建轮播图
     */
    @PostMapping
    @Operation(summary = "创建轮播图", description = "创建新的轮播图")
    public Boolean createBanner(@Valid @RequestBody BannerCreateDTO createDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}创建轮播图，标题：{}", adminId, createDTO.getTitle());
        return bannerManageService.createBanner(createDTO);
    }

    /**
     * 更新轮播图
     */
    @PutMapping
    @Operation(summary = "更新轮播图", description = "更新轮播图信息")
    public Boolean updateBanner(@Valid @RequestBody BannerUpdateDTO updateDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}更新轮播图，轮播图ID：{}", adminId, updateDTO.getBannerId());
        return bannerManageService.updateBanner(updateDTO);
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/{bannerId}")
    @Operation(summary = "删除轮播图", description = "根据ID删除轮播图")
    public Boolean deleteBanner(@PathVariable Long bannerId) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}删除轮播图，轮播图ID：{}", adminId, bannerId);
        return bannerManageService.deleteBanner(bannerId);
    }

    /**
     * 启用/禁用轮播图
     */
    @PutMapping("/{bannerId}/status")
    @Operation(summary = "启用/禁用轮播图", description = "更新轮播图状态")
    public Boolean updateBannerStatus(
            @PathVariable Long bannerId,
            @RequestParam Integer status) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}更新轮播图状态，轮播图ID：{}，状态：{}", adminId, bannerId, status);
        return bannerManageService.updateBannerStatus(bannerId, status);
    }
}
