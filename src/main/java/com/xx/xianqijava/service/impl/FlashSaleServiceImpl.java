package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.FlashSaleActivity;
import com.xx.xianqijava.entity.FlashSaleProduct;
import com.xx.xianqijava.entity.FlashSaleProductExt;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.entity.FlashSaleOrderExt;
import com.xx.xianqijava.entity.FlashSaleProductRelation;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.enums.FlashSaleStatus;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.FlashSaleActivityMapper;
import com.xx.xianqijava.mapper.FlashSaleOrderExtMapper;
import com.xx.xianqijava.mapper.FlashSaleProductExtMapper;
import com.xx.xianqijava.mapper.FlashSaleProductMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionMapper;
import com.xx.xianqijava.mapper.FlashSaleProductRelationMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.service.FlashSaleService;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.FlashSaleSessionVO;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀服务实现类（优化版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl extends ServiceImpl<FlashSaleActivityMapper, FlashSaleActivity> implements FlashSaleService {

    private final FlashSaleActivityMapper activityMapper;
    private final FlashSaleProductMapper flashProductMapper;
    private final ProductMapper productMapper;
    private final FlashSaleProductExtMapper flashSaleProductExtMapper;
    private final FlashSaleSessionMapper flashSaleSessionMapper;
    private final FlashSaleOrderExtMapper flashSaleOrderExtMapper;
    private final FlashSaleProductRelationMapper flashSaleProductRelationMapper;

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
            vo.setTitle(product.getTitle());
            vo.setCoverImage(getProductCoverImage(product.getProductId()));
            vo.setOriginalPrice(product.getPrice());

            // 计算折扣 (转换为整数，如8表示8折)
            if (flashProduct.getFlashPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discount = flashProduct.getFlashPrice()
                        .divide(product.getPrice(), 1, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.TEN);
                vo.setDiscount(discount.intValue());
            }
        }

        // 获取活动结束时间
        FlashSaleActivity activity = activityMapper.selectById(flashProduct.getActivityId());
        if (activity != null) {
            vo.setEndTime(activity.getEndTime().toString());
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

    // ========== 新增方法实现 ==========

    @Override
    public List<FlashSaleSessionVO> getActiveSessions() {
        LocalDateTime now = LocalDateTime.now();

        // 获取当前活动
        FlashSaleActivity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return List.of();
        }

        // 查询该活动的所有场次
        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleSession::getActivityId, currentActivity.getActivityId())
               .orderByAsc(FlashSaleSession::getStartTime);

        List<FlashSaleSession> sessions = flashSaleSessionMapper.selectList(wrapper);

        // 转换为 VO
        return sessions.stream()
                .map(session -> {
                    FlashSaleSessionVO vo = new FlashSaleSessionVO();
                    vo.setSessionId(session.getSessionId());

                    // 格式化时间 (HH:mm)
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    vo.setTime(session.getSessionTime().format(formatter));
                    vo.setStartTime(session.getStartTime().toString());
                    vo.setEndTime(session.getEndTime().toString());

                    // 判断状态
                    if (now.isBefore(session.getStartTime())) {
                        vo.setStatus("upcoming");
                        vo.setProgress(0);
                    } else if (now.isAfter(session.getEndTime())) {
                        vo.setStatus("ended");
                        vo.setProgress(100);
                    } else {
                        vo.setStatus("ongoing");
                        // 计算进度百分比
                        long total = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toSeconds();
                        long elapsed = java.time.Duration.between(session.getStartTime(), now).toSeconds();
                        vo.setProgress((int) (elapsed * 100 / total));
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleProductVO> getSessionProducts(Long sessionId, Integer page, Integer pageSize) {
        // 分页参数
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;

        // 查询场次商品关联
        LambdaQueryWrapper<FlashSaleProductRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleProductRelation::getSessionId, sessionId)
               .orderByDesc(FlashSaleProductRelation::getSortOrder)
               .last("LIMIT " + limit + " OFFSET " + offset);

        List<FlashSaleProductRelation> relations = flashSaleProductRelationMapper.selectList(wrapper);

        return relations.stream()
                .map(this::convertRelationToProductVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canBuy(Long userId, Long productId, Long activityId) {
        // 检查商品是否存在且库存充足
        FlashSaleProductExt ext = flashSaleProductExtMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProductExt>()
                        .eq(FlashSaleProductExt::getProductId, productId)
        );

        if (ext == null || ext.getStockCount() <= ext.getSoldCount()) {
            return false;
        }

        // 检查用户购买限制
        int userBuyCount = getUserBuyCount(userId, activityId);
        return userBuyCount < ext.getLimitPerUser();
    }

    @Override
    public int getUserBuyCount(Long userId, Long activityId) {
        // 查询用户在该活动中的购买数量
        return flashSaleOrderExtMapper.selectCount(
                new LambdaQueryWrapper<FlashSaleOrderExt>()
                        .eq(FlashSaleOrderExt::getUserId, userId)
                        .eq(FlashSaleOrderExt::getActivityId, activityId)
        ).intValue();
    }

    @Override
    public FlashSaleProductVO getFlashSaleProductDetail(Long productId) {
        // 查询商品扩展信息
        FlashSaleProductExt ext = flashSaleProductExtMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProductExt>()
                        .eq(FlashSaleProductExt::getProductId, productId)
        );

        if (ext == null) {
            return null;
        }

        // 查询商品基础信息
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return null;
        }

        // 创建 VO
        FlashSaleProductVO vo = new FlashSaleProductVO();
        vo.setId(product.getProductId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setCoverImage(getProductCoverImage(productId));

        // 价格信息
        vo.setSeckillPrice(ext.getFlashPrice());
        vo.setOriginalPrice(ext.getOriginalPrice());
        vo.setPrice(product.getPrice());

        // 计算折扣
        if (ext.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = ext.getFlashPrice()
                    .divide(ext.getOriginalPrice(), 1, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN);
            vo.setDiscount(discount.intValue());
        }

        // 库存信息
        vo.setStock(ext.getStockCount());
        vo.setSoldCount(ext.getSoldCount());
        if (ext.getStockCount() > 0) {
            vo.setSoldPercent(ext.getSoldCount() * 100 / ext.getStockCount());
        }

        // 时间信息
        vo.setEndTime(ext.getEndTime().toString());

        // 其他信息
        vo.setLimitPerUser(ext.getLimitPerUser());
        vo.setLocation(product.getLocation());
        vo.setCondition(product.getConditionLevel() != null ? product.getConditionLevel().toString() : "");
        vo.setCategoryId(product.getCategoryId() != null ? product.getCategoryId().intValue() : null);

        // 状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(ext.getStartTime())) {
            vo.setStatus("upcoming");
        } else if (now.isAfter(ext.getEndTime())) {
            vo.setStatus("ended");
        } else {
            vo.setStatus("ongoing");
        }

        return vo;
    }

    /**
     * 将关联关系转换为 FlashSaleProductVO
     */
    private FlashSaleProductVO convertRelationToProductVO(FlashSaleProductRelation relation) {
        // 查询商品扩展信息
        FlashSaleProductExt ext = flashSaleProductExtMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProductExt>()
                        .eq(FlashSaleProductExt::getProductId, relation.getProductId())
        );

        // 如果关联表中有覆盖配置，使用关联表的配置
        BigDecimal flashPrice = relation.getFlashPrice() != null
                ? relation.getFlashPrice()
                : (ext != null ? ext.getFlashPrice() : BigDecimal.ZERO);
        Integer stockCount = relation.getStockCount() != null
                ? relation.getStockCount()
                : (ext != null ? ext.getStockCount() : 0);
        Integer soldCount = ext != null ? ext.getSoldCount() : 0;

        // 查询商品信息
        Product product = productMapper.selectById(relation.getProductId());
        if (product == null) {
            return null;
        }

        // 创建 VO
        FlashSaleProductVO vo = new FlashSaleProductVO();
        vo.setId(product.getProductId());
        vo.setTitle(product.getTitle());
        vo.setDescription(product.getDescription());
        vo.setCoverImage(getProductCoverImage(relation.getProductId()));

        // 价格信息
        vo.setSeckillPrice(flashPrice);
        vo.setOriginalPrice(product.getPrice());
        vo.setPrice(product.getPrice());

        // 计算折扣
        if (product.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = flashPrice
                    .divide(product.getPrice(), 1, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN);
            vo.setDiscount(discount.intValue());
        }

        // 库存信息
        vo.setStock(stockCount);
        vo.setSoldCount(soldCount);
        if (stockCount > 0) {
            vo.setSoldPercent(soldCount * 100 / stockCount);
        }

        // 限制
        vo.setLimitPerUser(ext != null ? ext.getLimitPerUser() : 1);

        // 其他信息
        vo.setLocation(product.getLocation());
        vo.setCondition(product.getConditionLevel() != null ? product.getConditionLevel().toString() : "");
        vo.setCategoryId(product.getCategoryId() != null ? product.getCategoryId().intValue() : null);

        return vo;
    }
}
