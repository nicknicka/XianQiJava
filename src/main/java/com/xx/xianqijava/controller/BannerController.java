package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.BannerService;
import com.xx.xianqijava.vo.BannerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 */
@Slf4j
@Tag(name = "轮播图管理")
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 获取启用的轮播图列表
     */
    @GetMapping
    @Operation(summary = "获取启用的轮播图列表")
    public Result<List<BannerVO>> getActiveBanners() {
        log.info("查询启用的轮播图列表");
        List<BannerVO> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }

    /**
     * 增加轮播图点击次数
     */
    @PostMapping("/{bannerId}/click")
    @Operation(summary = "增加轮播图点击次数")
    public Result<Void> incrementClickCount(
            @Parameter(description = "轮播图ID") @PathVariable("bannerId") Long bannerId) {
        log.info("增加轮播图点击次数, bannerId={}", bannerId);
        bannerService.incrementClickCount(bannerId);
        return Result.success("点击次数已记录");
    }
}
