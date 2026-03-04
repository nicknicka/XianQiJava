package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ProductAuditDTO;
import com.xx.xianqijava.dto.ProductCreateDTO;
import com.xx.xianqijava.dto.ProductDraftSaveDTO;
import com.xx.xianqijava.dto.ProductUpdateDTO;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.entity.FlashSaleProduct;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.entity.ProductImage;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.entity.ProductStatistics;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.mapper.FlashSaleProductMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.ProductImageMapper;
import com.xx.xianqijava.mapper.ProductStatisticsMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.service.ProductFavoriteService;
import com.xx.xianqijava.service.ProductViewHistoryService;
import com.xx.xianqijava.vo.ProductAuditVO;
import com.xx.xianqijava.vo.ProductDraftVO;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ProductStatisticsMapper productStatisticsMapper;
    private final FlashSaleProductMapper flashSaleProductMapper;
    private final FlashSaleSessionMapper flashSaleSessionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO createProduct(ProductCreateDTO createDTO, Long userId) {
        log.info("创建商品, userId={}, title={}, isFlashSale={}",
                 userId, createDTO.getTitle(), createDTO.getIsFlashSale());

        // 校验分类是否存在
        Category category = categoryMapper.selectById(createDTO.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 如果开启秒杀，校验秒杀配置
        if (createDTO.getIsFlashSale() != null && createDTO.getIsFlashSale()) {
            // 校验场次ID
            if (createDTO.getSessionId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "秒杀场次不能为空");
            }
            FlashSaleSession session = flashSaleSessionMapper.selectById(createDTO.getSessionId());
            if (session == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "秒杀场次不存在");
            }
            if (session.getEnabled() == null || session.getEnabled() != 1) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "秒杀场次未启用");
            }

            // 校验秒杀价格
            if (createDTO.getFlashPrice() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "秒杀价格不能为空");
            }
            if (createDTO.getFlashPrice().compareTo(createDTO.getPrice()) >= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "秒杀价格必须低于商品价格");
            }

            // 校验秒杀库存
            if (createDTO.getFlashSaleStock() == null || createDTO.getFlashSaleStock() <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "秒杀库存必须大于0");
            }

            // 如果选择"仅一次"，校验秒杀日期
            if (createDTO.getRepeatType() != null && createDTO.getRepeatType() == 0) {
                if (createDTO.getSaleDate() == null) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "选择'仅一次'时必须指定秒杀日期");
                }
            }
        }

        // 创建商品
        Product product = new Product();
        BeanUtil.copyProperties(createDTO, product);
        product.setSellerId(userId);
        product.setStatus(0); // 默认下架（待审核）
        product.setAuditStatus(0); // 待审核
        // 注意：viewCount 和 favoriteCount 现在由 product_statistics 表维护，不需要在这里设置

        boolean saved = save(product);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "商品创建失败");
        }

        log.info("商品创建成功, productId={}", product.getProductId());

        // 如果开启秒杀，创建秒杀商品记录
        if (createDTO.getIsFlashSale() != null && createDTO.getIsFlashSale()) {
            createFlashSaleProduct(product.getProductId(), createDTO);
        }

        return convertToVO(product, userId);
    }

    /**
     * 创建秒杀商品记录
     */
    private void createFlashSaleProduct(Long productId, ProductCreateDTO createDTO) {
        log.info("创建秒杀商品记录, productId={}, sessionId={}", productId, createDTO.getSessionId());

        FlashSaleProduct flashProduct = new FlashSaleProduct();
        flashProduct.setProductId(productId);
        flashProduct.setSessionId(createDTO.getSessionId());
        flashProduct.setFlashPrice(createDTO.getFlashPrice());
        flashProduct.setStockCount(createDTO.getFlashSaleStock());
        flashProduct.setSoldCount(0);
        flashProduct.setLimitPerUser(createDTO.getLimitPerUser() != null ? createDTO.getLimitPerUser() : 1);
        flashProduct.setRepeatType(createDTO.getRepeatType() != null ? createDTO.getRepeatType() : 1); // 默认每日重复
        flashProduct.setSaleDate(createDTO.getSaleDate());
        flashProduct.setStockStatus(0); // 在售
        flashProduct.setSortOrder(0);
        flashProduct.setDeleted(0);

        boolean saved = flashSaleProductMapper.insert(flashProduct) > 0;
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "秒杀商品创建失败");
        }

        log.info("秒杀商品创建成功, id={}", flashProduct.getId());
    }

    @Override
    public ProductVO getProductDetail(Long productId, Long userId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 异步记录浏览历史（触发器会自动更新 product_statistics 表的 view_count）
        productViewHistoryService.recordViewHistory(userId, productId);

        return convertToVO(product, userId);
    }

    @Override
    public ProductVO getProductDetail(Long productId, Long userId, String channel) {
        // 如果是秒杀渠道，需要从 FlashSaleProduct 获取额外信息
        if ("flash".equals(channel)) {
            return getFlashSaleProductDetail(productId, userId);
        }
        // 默认渠道
        return getProductDetail(productId, userId);
    }

    /**
     * 获取秒杀商品详情
     */
    private ProductVO getFlashSaleProductDetail(Long productId, Long userId) {
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 异步记录浏览历史（触发器会自动更新 product_statistics 表的 view_count）
        productViewHistoryService.recordViewHistory(userId, productId);

        // 转换为基础 VO
        ProductVO productVO = convertToVO(product, userId);

        // 查询秒杀信息
        // 查询当前正在进行的活动
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> flashSaleInfo = baseMapper.selectFlashSaleInfo(productId, now);
        if (flashSaleInfo != null && !flashSaleInfo.isEmpty()) {
            // 设置秒杀相关字段
            productVO.setIsFlashSale(true);
            productVO.setSeckillPrice((BigDecimal) flashSaleInfo.get("seckill_price"));
            productVO.setFlashPrice((BigDecimal) flashSaleInfo.get("seckill_price"));
            productVO.setFlashSaleStock((Integer) flashSaleInfo.get("flash_sale_stock"));
            Object soldCount = flashSaleInfo.get("sold_count");
            productVO.setFlashSaleSold(soldCount != null ? ((Number) soldCount).intValue() : 0);
            productVO.setLimitPerUser((Integer) flashSaleInfo.get("limit_per_user"));

            // 计算折扣
            BigDecimal originalPrice = product.getPrice();
            BigDecimal seckillPrice = (BigDecimal) flashSaleInfo.get("seckill_price");
            if (originalPrice != null && seckillPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
                int discount = seckillPrice.multiply(BigDecimal.TEN)
                        .divide(originalPrice, 0, RoundingMode.HALF_UP).intValue();
                productVO.setDiscount(discount);

                // 计算已抢百分比
                Integer flashSaleStock = (Integer) flashSaleInfo.get("flash_sale_stock");
                int soldCountInt = soldCount != null ? ((Number) soldCount).intValue() : 0;
                if (flashSaleStock != null && flashSaleStock > 0) {
                    int soldPercent = (soldCountInt * 100) / flashSaleStock;
                    productVO.setSoldPercent(Math.min(soldPercent, 100));
                }
            }

            // 设置结束时间
            Object endTime = flashSaleInfo.get("end_time");
            if (endTime != null) {
                productVO.setEndTime(endTime.toString());
            }

            // 设置活动ID
            Object activityId = flashSaleInfo.get("activity_id");
            if (activityId != null) {
                productVO.setActivityId(((Number) activityId).longValue());
            }
        } else {
            productVO.setIsFlashSale(false);
        }

        return productVO;
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
        product.setConditionLevel(updateDTO.getConditionLevel());

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

        // 从 product_statistics 表获取统计数据
        ProductStatistics statistics = productStatisticsMapper.selectById(product.getProductId());
        if (statistics != null) {
            vo.setViewCount(statistics.getViewCount());
            vo.setFavoriteCount(statistics.getFavoriteCount());
        } else {
            // 如果统计数据不存在，使用默认值
            vo.setViewCount(0);
            vo.setFavoriteCount(0);
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
        return switch (auditStatus) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "审核拒绝";
            default -> "未知状态";
        };
    }

    /**
     * 获取商品状态描述
     */
    private String getProductStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "下架";
            case 1 -> "在售";
            case 2 -> "已售";
            case 3 -> "预订";
            default -> "未知状态";
        };
    }

    /**
     * 获取成色描述
     */
    private String getConditionDesc(Integer conditionLevel) {
        if (conditionLevel == null) {
            return "未描述";
        }
        return switch (conditionLevel) {
            case 10 -> "全新";
            case 9 -> "几乎全新";
            case 8 -> "轻微使用痕迹";
            case 7 -> "明显使用痕迹";
            case 6 -> "外观成色一般";
            default -> conditionLevel + "成新";
        };
    }

    @Override
    public int countByUserId(Long userId) {
        return Math.toIntExact(lambdaQuery()
                .eq(Product::getSellerId, userId)
                .eq(Product::getDeleted, 0)
                .count());
    }

    @Override
    public java.util.List<ProductVO> getRecentProductsByUserId(Long userId, int limit) {
        java.util.List<Product> products = lambdaQuery()
                .eq(Product::getSellerId, userId)
                .eq(Product::getDeleted, 0)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit)
                .list();

        return products.stream()
                .map(product -> convertToVO(product, userId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public IPage<ProductVO> getMyProducts(Page<Product> page, Long userId, Integer status) {
        log.info("获取我的商品列表, userId={}, page={}, status={}", userId, page.getCurrent(), status);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId);
        wrapper.eq(Product::getDeleted, 0);

        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = page(page, wrapper);
        return productPage.convert(product -> convertToVO(product, userId));
    }

    @Override
    public java.util.List<ProductVO> getSimilarProducts(Long productId, int limit) {
        log.info("获取相似商品列表, productId={}, limit={}", productId, limit);

        // 获取当前商品信息
        Product product = getById(productId);
        if (product == null) {
            log.warn("商品不存在, productId={}", productId);
            return java.util.Collections.emptyList();
        }

        // 查询相似商品：同分类、同状态、未删除、不是当前商品
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, product.getCategoryId())
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0)
                .ne(Product::getProductId, productId)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit);

        return list(wrapper).stream()
                .map(p -> convertToVO(p, null))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public IPage<ProductVO> getSellerProducts(Page<Product> page, Long userId, Long excludeProductId) {
        log.info("获取卖家的其他商品, userId={}, excludeProductId={}, page={}", userId, excludeProductId, page.getCurrent());

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId)
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0);

        if (excludeProductId != null) {
            wrapper.ne(Product::getProductId, excludeProductId);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = page(page, wrapper);
        return productPage.convert(product -> convertToVO(product, userId));
    }

    // ==================== 草稿相关方法实现 ====================

    private static final int MAX_DRAFT_COUNT = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductDraftVO saveDraft(ProductDraftSaveDTO draftDTO, Long userId) {
        log.info("保存商品草稿, userId={}, draftId={}", userId, draftDTO.getDraftId());

        // 1. 如果是新草稿，检查数量限制
        if (draftDTO.getDraftId() == null) {
            int currentDraftCount = countUserDrafts(userId);
            if (currentDraftCount >= MAX_DRAFT_COUNT) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "草稿数量已达上限（" + MAX_DRAFT_COUNT + "个），请先发布或删除部分草稿");
            }
        }

        // 2. 验证分类是否存在（如果提供了分类ID）
        if (draftDTO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(draftDTO.getCategoryId());
            if (category == null || category.getDeleted() == 1) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        Product product;
        boolean isUpdate = draftDTO.getDraftId() != null;

        if (isUpdate) {
            // 更新现有草稿
            product = getById(draftDTO.getDraftId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (!product.getSellerId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            if (!product.isDraft()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "只能修改草稿状态的记录");
            }
        } else {
            // 创建新草稿
            product = new Product();
            product.setSellerId(userId);
            product.setAsDraft();
            product.setAuditStatus(0); // 待审核
        }

        // 3. 更新字段（只更新非空字段）
        if (draftDTO.getTitle() != null) {
            product.setTitle(draftDTO.getTitle());
        }
        if (draftDTO.getDescription() != null) {
            product.setDescription(draftDTO.getDescription());
        }
        if (draftDTO.getCategoryId() != null) {
            product.setCategoryId(draftDTO.getCategoryId().longValue());
        }
        if (draftDTO.getPrice() != null) {
            product.setPrice(draftDTO.getPrice());
        }
        if (draftDTO.getOriginalPrice() != null) {
            product.setOriginalPrice(draftDTO.getOriginalPrice());
        }
        if (draftDTO.getConditionLevel() != null) {
            product.setConditionLevel(draftDTO.getConditionLevel());
        }
        if (draftDTO.getLocation() != null) {
            product.setLocation(draftDTO.getLocation());
        }
        if (draftDTO.getLatitude() != null) {
            product.setLatitude(draftDTO.getLatitude());
        }
        if (draftDTO.getLongitude() != null) {
            product.setLongitude(draftDTO.getLongitude());
        }

        // 4. 保存商品
        boolean saved = isUpdate ? updateById(product) : save(product);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "草稿保存失败");
        }

        // 5. 处理图片（草稿也支持图片上传）
        if (draftDTO.getImageUrls() != null && draftDTO.getImageUrls().length > 0) {
            saveProductImages(product.getProductId(), draftDTO.getImageUrls());
        }

        log.info("草稿保存成功, productId={}", product.getProductId());
        return convertToDraftVO(product);
    }

    @Override
    public IPage<ProductDraftVO> getDraftList(Page<Product> page, Long userId) {
        log.info("获取用户草稿列表, userId={}, page={}", userId, page.getCurrent());

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId);
        wrapper.eq(Product::getStatus, Product.STATUS_DRAFT);
        wrapper.eq(Product::getDeleted, 0);
        wrapper.orderByDesc(Product::getUpdateTime);

        IPage<Product> draftPage = page(page, wrapper);
        return draftPage.convert(this::convertToDraftVO);
    }

    @Override
    public ProductDraftVO getDraftDetail(Long draftId, Long userId) {
        log.info("获取草稿详情, draftId={}, userId={}", draftId, userId);

        Product product = getById(draftId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!product.isDraft()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该记录不是草稿");
        }

        return convertToDraftVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO publishFromDraft(Long draftId, Long userId) {
        log.info("从草稿发布商品, draftId={}, userId={}", draftId, userId);

        Product product = getById(draftId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!product.isDraft()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该记录不是草稿");
        }

        // 验证必填字段
        validateRequiredFields(product);

        // 更新状态为下架（待审核）
        product.setStatus(Product.STATUS_OFFLINE);
        product.setAuditStatus(0); // 待审核

        boolean updated = updateById(product);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "发布失败");
        }

        log.info("草稿发布成功, productId={}", draftId);
        return convertToVO(product, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long draftId, Long userId) {
        log.info("删除草稿, draftId={}, userId={}", draftId, userId);

        Product product = getById(draftId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!product.isDraft()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能删除草稿状态的记录");
        }

        // 逻辑删除
        boolean deleted = removeById(draftId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "草稿删除失败");
        }

        // 删除关联图片
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, draftId);
        productImageMapper.delete(imageWrapper);

        log.info("草稿删除成功");
    }

    @Override
    public int countUserDrafts(Long userId) {
        return Math.toIntExact(lambdaQuery()
                .eq(Product::getSellerId, userId)
                .eq(Product::getStatus, Product.STATUS_DRAFT)
                .eq(Product::getDeleted, 0)
                .count());
    }

    /**
     * 验证发布时的必填字段
     */
    private void validateRequiredFields(Product product) {
        List<String> missingFields = new java.util.ArrayList<>();

        if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
            missingFields.add("商品标题");
        }
        if (product.getCategoryId() == null) {
            missingFields.add("分类");
        }
        if (product.getPrice() == null) {
            missingFields.add("价格");
        }
        if (product.getConditionLevel() == null) {
            missingFields.add("成色");
        }

        if (!missingFields.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                "发布前请完善以下必填信息：" + String.join("、", missingFields));
        }
    }

    /**
     * 保存商品图片
     */
    private void saveProductImages(Long productId, String[] imageUrls) {
        // 删除旧图片
        LambdaQueryWrapper<ProductImage> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProductImage::getProductId, productId);
        productImageMapper.delete(deleteWrapper);

        // 保存新图片
        for (int i = 0; i < imageUrls.length && i < 9; i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls[i]);
            image.setSortOrder(i);
            image.setIsCover(i == 0 ? 1 : 0); // 第一张为封面
            image.setStatus(0);
            productImageMapper.insert(image);
        }
    }

    /**
     * 转换为草稿VO
     */
    private ProductDraftVO convertToDraftVO(Product product) {
        ProductDraftVO vo = new ProductDraftVO();
        BeanUtil.copyProperties(product, vo);
        vo.setDraftId(product.getProductId());
        vo.setProductId(product.getProductId());

        // 格式化时间
        if (product.getCreateTime() != null) {
            vo.setCreateTime(product.getCreateTime().toString());
        }
        if (product.getUpdateTime() != null) {
            vo.setUpdateTime(product.getUpdateTime().toString());
        }

        // 获取分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 获取图片
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, product.getProductId())
                .eq(ProductImage::getStatus, 0)
                .orderByAsc(ProductImage::getSortOrder);
        List<ProductImage> images = productImageMapper.selectList(imageWrapper);

        if (!images.isEmpty()) {
            String[] imageUrls = images.stream()
                    .map(ProductImage::getImageUrl)
                    .toArray(String[]::new);
            vo.setImages(imageUrls);
            vo.setImageCount(images.size());
            vo.setCoverImage(images.get(0).getImageUrl());
        }

        // 计算完成度和缺失字段
        vo.setCompletion(calculateCompletion(product));
        vo.setMissingFields(getMissingFields(product));

        return vo;
    }

    /**
     * 计算草稿完成度（0-100）
     */
    private Integer calculateCompletion(Product product) {
        int totalFields = 6; // 标题、描述、分类、价格、成色、图片
        int completedFields = 0;

        if (product.getTitle() != null && !product.getTitle().trim().isEmpty()) {
            completedFields++;
        }
        if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
            completedFields++;
        }
        if (product.getCategoryId() != null) {
            completedFields++;
        }
        if (product.getPrice() != null) {
            completedFields++;
        }
        if (product.getConditionLevel() != null) {
            completedFields++;
        }

        // 检查是否有图片
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, product.getProductId())
                .eq(ProductImage::getStatus, 0);
        long imageCount = productImageMapper.selectCount(imageWrapper);
        if (imageCount > 0) {
            completedFields++;
        }

        return (completedFields * 100) / totalFields;
    }

    /**
     * 获取缺失的必填字段列表
     */
    private String[] getMissingFields(Product product) {
        List<String> missingFields = new java.util.ArrayList<>();

        if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
            missingFields.add("商品标题");
        }
        if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
            missingFields.add("商品描述");
        }
        if (product.getCategoryId() == null) {
            missingFields.add("分类");
        }
        if (product.getPrice() == null) {
            missingFields.add("价格");
        }
        if (product.getConditionLevel() == null) {
            missingFields.add("成色");
        }

        // 检查是否有图片
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, product.getProductId())
                .eq(ProductImage::getStatus, 0);
        long imageCount = productImageMapper.selectCount(imageWrapper);
        if (imageCount == 0) {
            missingFields.add("商品图片");
        }

        return missingFields.toArray(new String[0]);
    }
}
