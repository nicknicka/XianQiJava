package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.Coupon;
import com.xx.xianqijava.entity.UserCoupon;
import com.xx.xianqijava.vo.CouponVO;
import com.xx.xianqijava.vo.UserCouponVO;

/**
 * 优惠券服务接口
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 获取可领取的优惠券列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 优惠券列表
     */
    IPage<CouponVO> getAvailableCoupons(Long userId, Page<Coupon> page);

    /**
     * 领取优惠券
     *
     * @param userId  用户ID
     * @param couponId 优惠券ID
     */
    void receiveCoupon(Long userId, Long couponId);

    /**
     * 获取用户优惠券列表
     *
     * @param userId 用户ID
     * @param status 优惠券状态：1-未使用，2-已使用，3-已过期
     * @param page   分页参数
     * @return 用户优惠券列表
     */
    IPage<UserCouponVO> getUserCoupons(Long userId, Integer status, Page<UserCoupon> page);

    /**
     * 使用优惠券
     *
     * @param userCouponId 用户优惠券ID
     * @param userId       用户ID
     * @param orderId      订单ID
     * @return 优惠金额
     */
    java.math.BigDecimal useCoupon(Long userCouponId, Long userId, Long orderId);

    /**
     * 获取优惠券详情
     *
     * @param couponId 优惠券ID
     * @param userId    用户ID
     * @return 优惠券VO
     */
    CouponVO getCouponDetail(Long couponId, Long userId);
}
