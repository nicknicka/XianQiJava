package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ProductCreateDTO;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.service.ProductFavoriteService;
import com.xx.xianqijava.service.ProductViewHistoryService;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        product.setStatus(1); // 默认在售
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

        // 增加浏览次数
        product.setViewCount(product.getViewCount() + 1);
        updateById(product);

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
}
