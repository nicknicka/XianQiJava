package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductFavorite;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ProductFavoriteMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.service.ProductFavoriteService;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品收藏服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductFavoriteServiceImpl extends ServiceImpl<ProductFavoriteMapper, ProductFavorite> implements ProductFavoriteService {

    private final ProductMapper productMapper;
    private final ProductService productService;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<ProductFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId);
        Long count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("已收藏该商品");
        }

        // 添加收藏
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        baseMapper.insert(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<ProductFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<ProductFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public IPage<ProductVO> getFavoriteList(Long userId, Page<ProductFavorite> page) {
        // 查询收藏记录
        IPage<ProductFavorite> favoritePage = baseMapper.selectPage(page,
                new LambdaQueryWrapper<ProductFavorite>()
                        .eq(ProductFavorite::getUserId, userId)
                        .orderByDesc(ProductFavorite::getCreateTime));

        // 转换为ProductVO，过滤掉已删除的商品
        java.util.List<ProductVO> validProducts = favoritePage.getRecords().stream()
                .map(favorite -> {
                    Product product = productMapper.selectById(favorite.getProductId());
                    if (product == null || product.getDeleted() == 1) {
                        return null;
                    }
                    return productService.convertToVO(product, userId);
                })
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        // 构建新的分页结果
        IPage<ProductVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), favoritePage.getTotal());
        resultPage.setRecords(validProducts);
        return resultPage;
    }
}
