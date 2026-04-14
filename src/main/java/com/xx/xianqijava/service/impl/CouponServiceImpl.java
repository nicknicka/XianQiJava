package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.Coupon;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.UserCoupon;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.CouponMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserCouponMapper;
import com.xx.xianqijava.service.CouponService;
import com.xx.xianqijava.util.IdConverter;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.CouponVO;
import com.xx.xianqijava.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final UserCouponMapper userCouponMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    public IPage<CouponVO> getAvailableCoupons(Long userId, Page<Coupon> page) {
        LocalDateTime now = LocalDateTime.now();

        // 查询进行中的优惠券
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coupon::getStatus, 1)
                .le(Coupon::getValidFrom, now)
                .ge(Coupon::getValidTo, now)
                .orderByDesc(Coupon::getSortOrder)
                .orderByDesc(Coupon::getCreateTime);

        IPage<Coupon> couponPage = baseMapper.selectPage(page, queryWrapper);

        return couponPage.convert(coupon -> convertToVO(coupon, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = baseMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不存在");
        }

        if (coupon.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不可领取");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不在有效期内");
        }

        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券已领完");
        }

        // 检查用户已领取数量
        Long userReceivedCount = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId)
        );

        if (userReceivedCount >= coupon.getLimitPerUser()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已达到领取上限");
        }

        // 创建用户优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(1); // 未使用
        userCoupon.setExpireTime(coupon.getValidTo());

        userCouponMapper.insert(userCoupon);

        // 更新优惠券已领取数量
        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        baseMapper.updateById(coupon);

        log.info("用户领取优惠券成功, userId={}, couponId={}", userId, couponId);
    }

    @Override
    public IPage<UserCouponVO> getUserCoupons(Long userId, Integer status, Page<UserCoupon> page) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId);

        if (status != null && status > 0) {
            queryWrapper.eq(UserCoupon::getStatus, status);
        }

        queryWrapper.orderByDesc(UserCoupon::getCreateTime);

        IPage<UserCoupon> userCouponPage = userCouponMapper.selectPage(page, queryWrapper);

        return userCouponPage.convert(this::convertToUserCouponVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal useCoupon(Long userCouponId, Long userId, Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不存在");
        }

        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限使用此优惠券");
        }

        if (userCoupon.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不可用");
        }

        if (LocalDateTime.now().isAfter(userCoupon.getExpireTime())) {
            // 标记为已过期
            userCoupon.setStatus(3);
            userCouponMapper.updateById(userCoupon);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券已过期");
        }

        Coupon coupon = baseMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不存在");
        }

        // 获取订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单不存在");
        }

        // 验证订单金额是否满足门槛
        validateOrderAmount(order, coupon);

        // 验证订单商品是否符合使用范围
        validateProductScope(order, coupon);

        // 计算优惠金额
        BigDecimal discountAmount = calculateDiscount(coupon, order);

        // 更新用户优惠券状态
        userCoupon.setStatus(2); // 已使用
        userCoupon.setUsedTime(LocalDateTime.now());
        userCoupon.setOrderId(orderId);
        userCouponMapper.updateById(userCoupon);

        // 更新优惠券已使用数量
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        baseMapper.updateById(coupon);

        log.info("使用优惠券成功, userCouponId={}, userId={}, orderId={}, discount={}",
                userCouponId, userId, orderId, discountAmount);

        return discountAmount;
    }

    @Override
    public CouponVO getCouponDetail(Long couponId, Long userId) {
        Coupon coupon = baseMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券不存在");
        }

        return convertToVO(coupon, userId);
    }

    /**
     * 计算优惠金额
     */
    private BigDecimal calculateDiscount(Coupon coupon, Order order) {
        BigDecimal orderAmount = order.getAmount();

        switch (coupon.getType()) {
            case 1: // 满减券
                return coupon.getDiscountValue();
            case 2: // 折扣券
                // discountValue 是折扣值，如 8.5 表示 8.5 折
                BigDecimal discount = coupon.getDiscountValue().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
                BigDecimal discountAmount = orderAmount.multiply(BigDecimal.ONE.subtract(discount))
                        .setScale(2, RoundingMode.HALF_UP);

                // 如果设置了最大优惠金额，不能超过最大优惠
                if (coupon.getMaxDiscount() != null && coupon.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    return discountAmount.min(coupon.getMaxDiscount());
                }
                return discountAmount;
            case 3: // 免邮券
                // 返回固定邮费减免金额或根据实际邮费计算
                return BigDecimal.valueOf(10); // 假设免邮10元
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 验证订单金额是否满足门槛
     */
    private void validateOrderAmount(Order order, Coupon coupon) {
        if (coupon.getMinAmount() != null && coupon.getMinAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (order.getAmount() == null || order.getAmount().compareTo(coupon.getMinAmount()) < 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        String.format("订单金额需满%s元才能使用此优惠券", coupon.getMinAmount()));
            }
        }
    }

    /**
     * 验证订单商品是否符合使用范围
     */
    private void validateProductScope(Order order, Coupon coupon) {
        // 如果使用范围是全场，则不需要验证
        if (coupon.getScope() == null || coupon.getScope() == 1) {
            return;
        }

        // 获取订单商品信息
        Long productId = order.getProductId();
        if (productId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单商品信息不完整");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不存在");
        }

        // 验证指定分类
        if (coupon.getScope() == 2) {
            if (StrUtil.isBlank(coupon.getCategoryIds())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券适用分类未设置");
            }

            JSONArray categoryArray = JSONUtil.parseArray(coupon.getCategoryIds());
            List<Long> categoryIds = new ArrayList<>();
            for (Object item : categoryArray) {
                if (item instanceof JSONObject) {
                    categoryIds.add(((JSONObject) item).getLong("id"));
                } else {
                    categoryIds.add(Long.valueOf(item.toString()));
                }
            }

            if (!categoryIds.contains(product.getCategoryId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "此优惠券仅适用于指定分类的商品");
            }
        }

        // 验证指定商品
        if (coupon.getScope() == 3) {
            if (StrUtil.isBlank(coupon.getProductIds())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "优惠券适用商品未设置");
            }

            JSONArray productArray = JSONUtil.parseArray(coupon.getProductIds());
            List<Long> productIds = new ArrayList<>();
            for (Object item : productArray) {
                if (item instanceof JSONObject) {
                    productIds.add(((JSONObject) item).getLong("id"));
                } else {
                    productIds.add(Long.valueOf(item.toString()));
                }
            }

            if (!productIds.contains(productId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "此优惠券仅适用于指定商品");
            }
        }
    }

    /**
     * 转换为优惠券VO
     */
    private CouponVO convertToVO(Coupon coupon, Long userId) {
        CouponVO vo = new CouponVO();
        vo.setCouponId(String.valueOf(coupon.getCouponId()));
        vo.setName(coupon.getName());
        vo.setDescription(coupon.getDescription());
        vo.setType(coupon.getType());
        vo.setTypeDesc(getTypeDesc(coupon.getType()));
        vo.setMinAmount(coupon.getMinAmount());
        vo.setDiscountValue(coupon.getDiscountValue());
        vo.setMaxDiscount(coupon.getMaxDiscount());

        // 计算折扣百分比
        if (coupon.getType() == 2) { // 折扣券
            if (coupon.getDiscountValue() != null) {
                vo.setDiscountPercent(coupon.getDiscountValue().divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP) + "折");
            }
        }

        vo.setTotalCount(coupon.getTotalCount());
        vo.setReceivedCount(coupon.getReceivedCount());
        vo.setUsedCount(coupon.getUsedCount());
        vo.setRemainingCount(coupon.getTotalCount() - coupon.getReceivedCount());
        vo.setLimitPerUser(coupon.getLimitPerUser());
        vo.setImageUrl(coupon.getImageUrl());
        vo.setValidFrom(coupon.getValidFrom());
        vo.setValidTo(coupon.getValidTo());
        vo.setStatus(coupon.getStatus());
        vo.setStatusDesc(getCouponStatusDesc(coupon.getStatus()));

        // 获取用户已领取数量
        if (userId != null) {
            Long userReceivedCount = userCouponMapper.selectCount(
                    new LambdaQueryWrapper<UserCoupon>()
                            .eq(UserCoupon::getUserId, userId)
                            .eq(UserCoupon::getCouponId, coupon.getCouponId())
            );
            vo.setUserReceivedCount(userReceivedCount.intValue());

            // 判断是否可领取
            boolean canReceive = true;
            String reason = null;

            if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
                canReceive = false;
                reason = "优惠券已领完";
            } else if (userReceivedCount >= coupon.getLimitPerUser()) {
                canReceive = false;
                reason = "已达到领取上限";
            } else if (coupon.getStatus() != 1) {
                canReceive = false;
                reason = "优惠券不可用";
            }

            vo.setCanReceive(canReceive);
            vo.setCannotReceiveReason(reason);
        }

        return vo;
    }

    /**
     * 转换为用户优惠券VO
     */
    private UserCouponVO convertToUserCouponVO(UserCoupon userCoupon) {
        Coupon coupon = baseMapper.selectById(userCoupon.getCouponId());

        UserCouponVO vo = new UserCouponVO();
        vo.setUserCouponId(String.valueOf(userCoupon.getUserCouponId()));
        vo.setCouponId(String.valueOf(userCoupon.getCouponId()));

        if (coupon != null) {
            vo.setName(coupon.getName());
            vo.setDescription(coupon.getDescription());
            vo.setType(coupon.getType());
            vo.setTypeDesc(getTypeDesc(coupon.getType()));
            vo.setMinAmount(coupon.getMinAmount());
            vo.setDiscountValue(coupon.getDiscountValue());
            vo.setMaxDiscount(coupon.getMaxDiscount());
            vo.setImageUrl(coupon.getImageUrl());

            if (coupon.getType() == 2) {
                if (coupon.getDiscountValue() != null) {
                    vo.setDiscountPercent(coupon.getDiscountValue().divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP) + "折");
                }
            }
        }

        vo.setStatus(userCoupon.getStatus());
        vo.setStatusDesc(getUserCouponStatusDesc(userCoupon.getStatus()));
        vo.setUsedTime(userCoupon.getUsedTime());
        vo.setExpireTime(userCoupon.getExpireTime());

        // 判断是否即将过期（3天内）
        if (userCoupon.getExpireTime() != null && userCoupon.getStatus() == 1) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), userCoupon.getExpireTime());
            vo.setExpiringSoon(daysUntilExpiry <= 3);
        } else {
            vo.setExpiringSoon(false);
        }

        return vo;
    }

    private String getTypeDesc(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "满减券";
            case 2: return "折扣券";
            case 3: return "免邮券";
            default: return "未知";
        }
    }

    private String getCouponStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "进行中";
            case 2: return "已结束";
            case 3: return "已作废";
            default: return "未知";
        }
    }

    private String getUserCouponStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "未使用";
            case 2: return "已使用";
            case 3: return "已过期";
            default: return "未知";
        }
    }
}
