package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ProductAuditDTO;
import com.xx.xianqijava.dto.ProductCreateDTO;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.entity.ProductImage;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.ProductImageMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.service.ProductFavoriteService;
import com.xx.xianqijava.service.ProductViewHistoryService;
import com.xx.xianqijava.vo.ProductAuditVO;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final ProductFavoriteService productFavoriteService;
    private final ProductViewHistoryService productViewHistoryService;
    private final ProductImageMapper productImageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO createProduct(ProductCreateDTO createDTO, Long userId) {
        log.info("创建商品, userId={}, title={}", userId, createDTO.getTitle());

        // 校验分类是否存在
        Category category = categoryMapper.selectById(createDTO.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 创建商品
        Product product = new Product();
        BeanUtil.copyProperties(createDTO, product);
        product.setSellerId(userId);
        product.setStatus(0); // 默认下架（待审核）
        product.setAuditStatus(0); // 待审核
        product.setViewCount(0);
        product.setFavoriteCount(0);

        boolean saved = save(product);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "商品创建失败");
        }

        log.info("商品创建成功, productId={}", product.getProductId());

        return convertToVO(product, userId);
    }

    @Override
    public ProductVO getProductDetail(Long productId, Long userId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 使用 SQL 直接增加浏览次数（避免并发问题）
        // 使用 MyBatis-Plus 的 UpdateWrapper 实现 SET view_count = view_count + 1
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.setSql("view_count = view_count + 1")
                .eq(Product::getProductId, productId);
        baseMapper.update(null, updateWrapper);

        // 异步记录浏览历史
        productViewHistoryService.recordViewHistory(userId, productId);

        return convertToVO(product, userId);
    }

    @Override
    public IPage<ProductVO> getProductList(Page<Product> page, Integer categoryId, String keyword) {
        IPage<Product> productPage = baseMapper.selectProductPage(page, categoryId, 1, keyword);
        
        return productPage.convert(product -> convertToVO(product, null));
    }

    @Override
    public IPage<ProductVO> searchProducts(Page<Product> page, String keyword, Integer categoryId,
                                          BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Product::getStatus, 1); // 只查询在售商品
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getTitle, keyword)
                           .or()
                           .like(Product::getDescription, keyword));
        }
        
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }
        
        wrapper.orderByDesc(Product::getCreateTime);
        
        IPage<Product> productPage = page(page, wrapper);
        
        return productPage.convert(product -> convertToVO(product, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long productId, Integer status, Long userId) {
        log.info("更新商品状态, productId={}, status={}, userId={}", productId, status, userId);

        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 只有卖家可以修改自己的商品
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此商品");
        }

        product.setStatus(status);
        boolean updated = updateById(product);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新商品状态失败");
        }

        log.info("商品状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId, Long userId) {
        log.info("删除商品, productId={}, userId={}", productId, userId);

        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 只有卖家可以删除自己的商品
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此商品");
        }

        // 已售出的商品不能删除
        if (product.getStatus() == 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已售出的商品不能删除");
        }

        boolean deleted = removeById(productId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "删除商品失败");
        }

        log.info("商品删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(Long productId, ProductUpdateDTO updateDTO, Long userId) {
        log.info("更新商品信息, productId={}, userId={}", productId, userId);

        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 只有卖家可以更新自己的商品
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此商品");
        }

        // 已售出的商品不能修改
        if (product.getStatus() == 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已售出的商品不能修改");
        }

        // 校验分类是否存在
        if (updateDTO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(updateDTO.getCategoryId());
            if (category == null || category.getDeleted() == 1) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            product.setCategoryId(updateDTO.getCategoryId());
        }

        // 更新商品信息
        product.setTitle(updateDTO.getTitle());
        product.setDescription(updateDTO.getDescription());
        product.setPrice(updateDTO.getPrice());
        product.setCondition(updateDTO.getCondition());

        if (updateDTO.getLocation() != null) {
            product.setLocation(updateDTO.getLocation());
        }
        if (updateDTO.getLatitude() != null) {
            product.setLatitude(updateDTO.getLatitude());
        }
        if (updateDTO.getLongitude() != null) {
            product.setLongitude(updateDTO.getLongitude());
        }

        boolean updated = updateById(product);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新商品失败");
        }

        log.info("商品信息更新成功, productId={}", productId);
        return convertToVO(product, userId);
    }

    /**
     * 转换为VO
     */
    public ProductVO convertToVO(Product product, Long currentUserId) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setCreateTime(product.getCreateTime().toString());

        // 获取卖家信息
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
            vo.setSellerCreditScore(seller.getCreditScore());
        }

        // 获取分类信息
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 检查是否已收藏
        if (currentUserId != null) {
            LambdaQueryWrapper<ProductFavorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductFavorite::getUserId, currentUserId)
                   .eq(ProductFavorite::getProductId, product.getProductId());
            boolean isFavorite = productFavoriteService.count(wrapper) > 0;
            vo.setIsFavorite(isFavorite);
        } else {
            vo.setIsFavorite(false);
        }

        return vo;
    }

    @Override
    public IPage<ProductVO> getNearbyProducts(Page<Product> page, Long userId,
                                              BigDecimal latitude, BigDecimal longitude, Integer radius) {
        log.info("获取附近商品, userId={}, latitude={}, longitude={}, radius={}",
                userId, latitude, longitude, radius);

        // 获取当前用户信息
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 只查询在售商品
        wrapper.isNotNull(Product::getLatitude); // 必须有纬度
        wrapper.isNotNull(Product::getLongitude); // 必须有经度

        // 计算距离并筛选附近商品（使用 Haversine 公式估算）
        // 这里使用简单的矩形范围筛选，实际应用中可使用更精确的地理空间查询
        // 1度经纬度约等于111公里
        if (latitude != null && longitude != null && radius != null && radius > 0) {
            double latDegreeRange = (double) radius / 111.0;
            double lonDegreeRange = (double) radius / (111.0 * Math.cos(Math.toRadians(latitude.doubleValue())));

            BigDecimal minLat = latitude.subtract(BigDecimal.valueOf(latDegreeRange));
            BigDecimal maxLat = latitude.add(BigDecimal.valueOf(latDegreeRange));
            BigDecimal minLon = longitude.subtract(BigDecimal.valueOf(lonDegreeRange));
            BigDecimal maxLon = longitude.add(BigDecimal.valueOf(lonDegreeRange));

            wrapper.ge(Product::getLatitude, minLat)
                   .le(Product::getLatitude, maxLat)
                   .ge(Product::getLongitude, minLon)
                   .le(Product::getLongitude, maxLon);
        }

        // 优先推荐同学院/同专业的用户的商品
        if (currentUser.getCollege() != null && !currentUser.getCollege().isEmpty()) {
            // 获取同学院用户列表
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getCollege, currentUser.getCollege())
                      .select(User::getUserId);
            List<Long> collegeUserIds = userMapper.selectList(userWrapper).stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());

            if (!collegeUserIds.isEmpty()) {
                wrapper.or().in(Product::getSellerId, collegeUserIds);
            }
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = page(page, wrapper);
        return productPage.convert(product -> convertToVO(product, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductAuditVO auditProduct(ProductAuditDTO auditDTO, Long auditorId) {
        log.info("审核商品, productId={}, auditStatus={}, auditorId={}",
                auditDTO.getProductId(), auditDTO.getAuditStatus(), auditorId);

        Product product = getById(auditDTO.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不存在");
        }

        // 更新审核状态
        product.setAuditStatus(auditDTO.getAuditStatus());
        product.setAuditRemark(auditDTO.getAuditRemark());
        product.setAuditTime(LocalDateTime.now());
        product.setAuditorId(auditorId);

        // 审核通过后，自动上架（如果商品是下架状态）
        if (auditDTO.getAuditStatus() == 1 && product.getStatus() == 0) {
            product.setStatus(1); // 设为在售
        }

        boolean updated = updateById(product);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "审核失败");
        }

        log.info("商品审核完成, productId={}, auditStatus={}", product.getProductId(), product.getAuditStatus());
        return convertToAuditVO(product);
    }

    @Override
    public IPage<ProductAuditVO> getPendingProducts(Page<Product> page) {
        log.info("查询待审核商品列表, page={}", page.getCurrent());

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getAuditStatus, 0) // 待审核
                .orderByAsc(Product::getCreateTime);

        IPage<Product> productPage = page(page, wrapper);
        return productPage.convert(this::convertToAuditVO);
    }

    @Override
    public IPage<ProductAuditVO> getAllProductAudits(Page<Product> page, Integer auditStatus) {
        log.info("查询所有商品审核列表, page={}, auditStatus={}", page.getCurrent(), auditStatus);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(Product::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = page(page, wrapper);
        return productPage.convert(this::convertToAuditVO);
    }

    @Override
    public ProductAuditVO getProductAuditDetail(Long productId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不存在");
        }
        return convertToAuditVO(product);
    }

    /**
     * 转换为审核VO
     */
    private ProductAuditVO convertToAuditVO(Product product) {
        ProductAuditVO vo = new ProductAuditVO();
        BeanUtil.copyProperties(product, vo);

        // 设置审核状态描述
        vo.setAuditStatusDesc(getAuditStatusDesc(product.getAuditStatus()));

        // 设置商品状态描述
        vo.setStatusDesc(getProductStatusDesc(product.getStatus()));

        // 设置成色描述
        vo.setConditionDesc(getConditionDesc(product.getConditionLevel()));

        // 设置时间
        if (product.getCreateTime() != null) {
            vo.setCreateTime(product.getCreateTime().toString());
        }
        if (product.getAuditTime() != null) {
            vo.setAuditTime(product.getAuditTime().toString());
        }

        // 获取卖家信息
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerPhone(seller.getPhone());
        }

        // 获取分类信息
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 获取商品图片
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, product.getProductId())
                .eq(ProductImage::getStatus, 0)
                .orderByAsc(ProductImage::getSortOrder);
        List<ProductImage> images = productImageMapper.selectList(imageWrapper);
        List<String> imageUrls = images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        vo.setImages(imageUrls);

        return vo;
    }

    /**
     * 获取审核状态描述
     */
    private String getAuditStatusDesc(Integer auditStatus) {
        switch (auditStatus) {
            case 0:
                return "待审核";
            case 1:
                return "审核通过";
            case 2:
                return "审核拒绝";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取商品状态描述
     */
    private String getProductStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "下架";
            case 1:
                return "在售";
            case 2:
                return "已售";
            case 3:
                return "预订";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取成色描述
     */
    private String getConditionDesc(Integer conditionLevel) {
        if (conditionLevel == null) {
            return "未描述";
        }
        switch (conditionLevel) {
            case 10:
                return "全新";
            case 9:
                return "几乎全新";
            case 8:
                return "轻微使用痕迹";
            case 7:
                return "明显使用痕迹";
            case 6:
                return "外观成色一般";
            default:
                return conditionLevel + "成新";
        }
    }
}
