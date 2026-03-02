package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Coupon;
import com.xx.xianqijava.entity.UserCoupon;
import com.xx.xianqijava.service.CouponService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.CouponVO;
import com.xx.xianqijava.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券控制器
 */
@Slf4j
@Tag(name = "优惠券管理")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * 获取可领取的优惠券列表
     */
    @Operation(summary = "获取可领取的优惠券列表")
    @GetMapping("/available")
    public Result<IPage<CouponVO>> getAvailableCoupons(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取可领取优惠券列表, userId={}, page={}, size={}", userId, page, size);

        Page<Coupon> pageParam = new Page<>(page, size);
        IPage<CouponVO> couponPage = couponService.getAvailableCoupons(userId, pageParam);

        return Result.success(couponPage);
    }

    /**
     * 领取优惠券
     */
    @Operation(summary = "领取优惠券")
    @PostMapping("/{couponId}/receive")
    public Result<Void> receiveCoupon(
            @Parameter(description = "优惠券ID") @PathVariable("couponId") Long couponId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("领取优惠券, userId={}, couponId={}", userId, couponId);
        couponService.receiveCoupon(userId, couponId);
        return Result.success("领取成功");
    }

    /**
     * 获取我的优惠券列表
     */
    @Operation(summary = "获取我的优惠券列表")
    @GetMapping("/my")
    public Result<IPage<UserCouponVO>> getMyCoupons(
            @Parameter(description = "优惠券状态：1-未使用，2-已使用，3-已过期") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取我的优惠券列表, userId={}, status={}, page={}, size={}", userId, status, page, size);

        Page<UserCoupon> pageParam = new Page<>(page, size);
        IPage<UserCouponVO> couponPage = couponService.getUserCoupons(userId, status, pageParam);

        return Result.success(couponPage);
    }

    /**
     * 获取优惠券详情
     */
    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{couponId}")
    public Result<CouponVO> getCouponDetail(
            @Parameter(description = "优惠券ID") @PathVariable("couponId") Long couponId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取优惠券详情, couponId={}, userId={}", couponId, userId);
        CouponVO couponVO = couponService.getCouponDetail(couponId, userId);
        return Result.success(couponVO);
    }
}
