package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.ProductAuditDTO;
import com.xx.xianqijava.dto.admin.ProductAuditQueryDTO;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ProductAuditService;
import com.xx.xianqijava.vo.admin.ProductAuditStatistics;
import com.xx.xianqijava.vo.admin.ProductAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品审核服务实现类 - 管理端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAuditServiceImpl implements ProductAuditService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<ProductAuditVO> getProductAuditList(ProductAuditQueryDTO queryDTO) {
        log.info("分页查询商品审核列表，查询条件：{}", queryDTO);

        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = buildQueryWrapper(queryDTO);

        // 分页查询
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Product> productPage = productMapper.selectPage(page, queryWrapper);

        // 转换为VO
        return convertToVOPage(productPage);
    }

    @Override
    public Page<ProductAuditVO> getPendingAuditList(ProductAuditQueryDTO queryDTO) {
        log.info("分页查询待审核商品列表，查询条件：{}", queryDTO);

        // 强制设置为待审核状态
        queryDTO.setAuditStatus(0);

        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = buildQueryWrapper(queryDTO);

        // 分页查询
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Product> productPage = productMapper.selectPage(page, queryWrapper);

        // 转换为VO
        return convertToVOPage(productPage);
    }

    @Override
    public ProductAuditVO getProductAuditDetail(Long productId) {
        log.info("获取商品审核详情，商品ID：{}", productId);

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        return convertToVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean auditProduct(ProductAuditDTO auditDTO) {
        log.info("审核商品，商品ID：{}，审核状态：{}", auditDTO.getProductId(), auditDTO.getAuditStatus());

        Product product = productMapper.selectById(auditDTO.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 拒绝时必须填写原因
        if (auditDTO.getAuditStatus() == 2 && !StringUtils.hasText(auditDTO.getAuditRemark())) {
            throw new RuntimeException("拒绝审核必须填写原因");
        }

        // 更新审核状态
        product.setAuditStatus(auditDTO.getAuditStatus());
        product.setAuditRemark(auditDTO.getAuditRemark());
        product.setAuditTime(LocalDateTime.now());
        product.setAuditorId(1L); // TODO: 从SecurityContext获取当前管理员ID

        // 审核通过，自动上架
        if (auditDTO.getAuditStatus() == 1) {
            product.setStatus(Product.STATUS_ON_SALE);
            log.info("商品审核通过，自动上架，商品ID：{}", auditDTO.getProductId());
        }

        int result = productMapper.updateById(product);

        log.info("审核商品{}，商品ID：{}", result > 0 ? "成功" : "失败", auditDTO.getProductId());
        return result > 0;
    }

    @Override
    public ProductAuditStatistics getAuditStatistics() {
        log.info("获取商品审核统计信息");

        ProductAuditStatistics statistics = new ProductAuditStatistics();

        // 总商品数
        Long totalProducts = productMapper.selectCount(null);
        statistics.setTotalProducts(totalProducts);

        // 待审核商品数
        Long pendingAuditCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getAuditStatus, 0)
        );
        statistics.setPendingAuditCount(pendingAuditCount);

        // 已通过商品数
        Long approvedCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getAuditStatus, 1)
        );
        statistics.setApprovedCount(approvedCount);

        // 已拒绝商品数
        Long rejectedCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getAuditStatus, 2)
        );
        statistics.setRejectedCount(rejectedCount);

        // 今日待审核商品数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todayPendingCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getAuditStatus, 0)
                        .ge(Product::getCreateTime, todayStart)
        );
        statistics.setTodayPendingCount(todayPendingCount);

        // 本周审核通过数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long weekApprovedCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getAuditStatus, 1)
                        .ge(Product::getAuditTime, weekStart)
        );
        statistics.setWeekApprovedCount(weekApprovedCount);

        // 本月审核通过数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        Long monthApprovedCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getAuditStatus, 1)
                        .ge(Product::getAuditTime, monthStart)
        );
        statistics.setMonthApprovedCount(monthApprovedCount);

        return statistics;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Product> buildQueryWrapper(ProductAuditQueryDTO queryDTO) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        // 商品标题模糊搜索
        if (StringUtils.hasText(queryDTO.getTitle())) {
            queryWrapper.like(Product::getTitle, queryDTO.getTitle());
        }

        // 卖家筛选
        if (queryDTO.getSellerId() != null) {
            queryWrapper.eq(Product::getSellerId, queryDTO.getSellerId());
        }

        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
        }

        // 审核状态筛选
        if (queryDTO.getAuditStatus() != null) {
            queryWrapper.eq(Product::getAuditStatus, queryDTO.getAuditStatus());
        }

        // 排序
        if ("price".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Product::getPrice);
        } else if ("auditTime".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Product::getAuditTime);
        } else {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Product::getCreateTime);
        }

        return queryWrapper;
    }

    /**
     * 转换Product分页数据为ProductAuditVO分页数据
     */
    private Page<ProductAuditVO> convertToVOPage(Page<Product> productPage) {
        // 批量获取用户信息
        List<Long> sellerIds = productPage.getRecords().stream()
                .map(Product::getSellerId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = sellerIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(sellerIds).stream()
                        .collect(Collectors.toMap(User::getUserId, u -> u));

        // 批量获取分类信息
        List<Long> categoryIds = productPage.getRecords().stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Category> categoryMap = categoryIds.isEmpty()
                ? Map.of()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getCategoryId, c -> c));

        // 转换为VO
        Page<ProductAuditVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<ProductAuditVO> voList = productPage.getRecords().stream()
                .map(product -> convertToVO(product, userMap, categoryMap))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 转换单个Product为ProductAuditVO
     */
    private ProductAuditVO convertToVO(Product product) {
        User seller = userMapper.selectById(product.getSellerId());
        Category category = categoryMapper.selectById(product.getCategoryId());

        Map<Long, User> userMap = seller != null ? Map.of(seller.getUserId(), seller) : Map.of();
        Map<Long, Category> categoryMap = category != null ? Map.of(category.getCategoryId(), category) : Map.of();

        return convertToVO(product, userMap, categoryMap);
    }

    /**
     * 转换Product为ProductAuditVO
     */
    private ProductAuditVO convertToVO(Product product, Map<Long, User> userMap, Map<Long, Category> categoryMap) {
        ProductAuditVO vo = new ProductAuditVO();
        BeanUtils.copyProperties(product, vo);

        // 设置卖家信息
        User seller = userMap.get(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerPhone(seller.getPhone());
        }

        // 设置分类信息
        Category category = categoryMap.get(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 设置成色（将10分制转换为5档）
        vo.setCondition(convertCondition(product.getConditionLevel()));

        return vo;
    }

    /**
     * 转换成色：10分制 -> 5档
     * 10分制: 1-10分（10为全新）
     * 5档制: 1-5档（1为全新，5为六成新及以下）
     */
    private Integer convertCondition(Integer conditionLevel) {
        if (conditionLevel == null) {
            return 5; // 默认为六成新及以下
        }
        if (conditionLevel >= 9) {
            return 1; // 9-10分 -> 全新
        } else if (conditionLevel >= 7) {
            return 2; // 7-8分 -> 九成新
        } else if (conditionLevel >= 5) {
            return 3; // 5-6分 -> 八成新
        } else if (conditionLevel >= 3) {
            return 4; // 3-4分 -> 七成新
        } else {
            return 5; // 1-2分 -> 六成新及以下
        }
    }
}
