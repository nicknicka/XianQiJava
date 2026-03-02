package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.FlashSaleActivity;
import com.xx.xianqijava.entity.FlashSaleProduct;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.enums.FlashSaleStatus;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.FlashSaleActivityMapper;
import com.xx.xianqijava.mapper.FlashSaleProductMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.service.FlashSaleService;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl extends ServiceImpl<FlashSaleActivityMapper, FlashSaleActivity> implements FlashSaleService {

    private final FlashSaleActivityMapper activityMapper;
    private final FlashSaleProductMapper flashProductMapper;
    private final ProductMapper productMapper;

    @Override
    public FlashSaleActivity getCurrentActivity() {
        LocalDateTime now = LocalDateTime.now();

        // 查询当前进行中的活动
        LambdaQueryWrapper<FlashSaleActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleActivity::getStatus, FlashSaleStatus.IN_PROGRESS.getCode())
               .le(FlashSaleActivity::getStartTime, now)
               .ge(FlashSaleActivity::getEndTime, now)
               .orderByDesc(FlashSaleActivity::getSortOrder)
               .last("LIMIT 1");

        return activityMapper.selectOne(wrapper);
    }

    @Override
    public List<ProductVO> getCurrentFlashSaleProducts(Integer limit) {
        // 获取当前活动
        FlashSaleActivity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return List.of();
        }

        // 查询活动商品
        LambdaQueryWrapper<FlashSaleProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleProduct::getActivityId, currentActivity.getActivityId())
               .orderByDesc(FlashSaleProduct::getSortOrder)
               .last("LIMIT " + (limit != null ? limit : 10));

        List<FlashSaleProduct> flashProducts = flashProductMapper.selectList(wrapper);

        // 转换为 ProductVO，包含秒杀价
        return flashProducts.stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleProductVO> getActivityProducts(Long activityId) {
        // 查询秒杀商品
        LambdaQueryWrapper<FlashSaleProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleProduct::getActivityId, activityId)
               .orderByDesc(FlashSaleProduct::getSortOrder);

        List<FlashSaleProduct> flashProducts = flashProductMapper.selectList(wrapper);

        return flashProducts.stream()
                .map(this::convertToFlashSaleProductVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 查询所有未结束的活动
        LambdaQueryWrapper<FlashSaleActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FlashSaleActivity::getStatus,
                   FlashSaleStatus.NOT_STARTED.getCode(),
                   FlashSaleStatus.IN_PROGRESS.getCode());

        List<FlashSaleActivity> activities = activityMapper.selectList(wrapper);

        for (FlashSaleActivity activity : activities) {
            Integer newStatus = null;

            if (now.isBefore(activity.getStartTime())) {
                // 未开始
                newStatus = FlashSaleStatus.NOT_STARTED.getCode();
            } else if (now.isAfter(activity.getEndTime())) {
                // 已结束
                newStatus = FlashSaleStatus.ENDED.getCode();
            } else {
                // 进行中
                newStatus = FlashSaleStatus.IN_PROGRESS.getCode();
            }

            if (!activity.getStatus().equals(newStatus)) {
                activity.setStatus(newStatus);
                activityMapper.updateById(activity);
                log.info("活动状态已更新, activityId={}, oldStatus={}, newStatus={}",
                         activity.getActivityId(), activity.getStatus(), newStatus);
            }
        }
    }

    /**
     * 转换为 ProductVO（包含秒杀价）
     */
    private ProductVO convertToProductVO(FlashSaleProduct flashProduct) {
        // 查询商品信息
        Product product = productMapper.selectById(flashProduct.getProductId());
        if (product == null) {
            log.warn("商品不存在, productId={}", flashProduct.getProductId());
            return null;
        }

        // 创建 ProductVO
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);

        // 设置秒杀相关信息
        vo.setFlashPrice(flashProduct.getFlashPrice());
        vo.setIsFlashSale(true);

        // 获取活动结束时间
        FlashSaleActivity activity = activityMapper.selectById(flashProduct.getActivityId());
        if (activity != null) {
            vo.setFlashEndTime(activity.getEndTime().toString());
        }

        return vo;
    }

    /**
     * 转换为 FlashSaleProductVO
     */
    private FlashSaleProductVO convertToFlashSaleProductVO(FlashSaleProduct flashProduct) {
        FlashSaleProductVO vo = new FlashSaleProductVO();
        BeanUtil.copyProperties(flashProduct, vo);

        // 查询商品信息
        Product product = productMapper.selectById(flashProduct.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductCoverImage(getProductCoverImage(product.getProductId()));
            vo.setOriginalPrice(product.getPrice());

            // 计算折扣
            if (flashProduct.getFlashPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discount = flashProduct.getFlashPrice()
                        .divide(product.getPrice(), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.TEN);
                vo.setDiscount(discount.toString() + "折");
            }
        }

        // 获取活动结束时间
        FlashSaleActivity activity = activityMapper.selectById(flashProduct.getActivityId());
        if (activity != null) {
            vo.setActivityEndTime(activity.getEndTime().toString());
        }

        return vo;
    }

    /**
     * 获取商品封面图（简化版）
     */
    private String getProductCoverImage(Long productId) {
        // TODO: 从 product_image 表查询封面图
        // 这里暂时返回空字符串，后续可以集成图片服务
        return "";
    }
}
