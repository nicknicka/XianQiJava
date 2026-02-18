package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ProductImageCreateDTO;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductImage;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.ProductImageMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.service.ProductImageService;
import com.xx.xianqijava.vo.ProductImageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品图片服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage>
        implements ProductImageService {

    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductImageVO addProductImage(Long productId, ProductImageCreateDTO imageCreateDTO, Long userId) {
        log.info("添加商品图片, productId={}, userId={}", productId, userId);

        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 只有卖家可以添加图片
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此商品");
        }

        // 检查图片数量限制（最多9张）
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                .eq(ProductImage::getStatus, 0);
        Long count = baseMapper.selectCount(queryWrapper);
        if (count >= 9) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品图片最多9张");
        }

        // 如果是第一张图片，自动设为封面
        if (count == 0 && imageCreateDTO.getIsCover() == null) {
            imageCreateDTO.setIsCover(1);
        }

        // 创建商品图片
        ProductImage productImage = new ProductImage();
        BeanUtil.copyProperties(imageCreateDTO, productImage);
        productImage.setProductId(productId);
        productImage.setStatus(0);

        // 如果设置为封面，取消其他图片的封面状态
        if (productImage.getIsCover() != null && productImage.getIsCover() == 1) {
            LambdaUpdateWrapper<ProductImage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ProductImage::getProductId, productId)
                    .set(ProductImage::getIsCover, 0);
            baseMapper.update(null, updateWrapper);
        }

        baseMapper.insert(productImage);

        log.info("商品图片添加成功, imageId={}", productImage.getImageId());
        return convertToVO(productImage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductImage(Long imageId, Long userId) {
        log.info("删除商品图片, imageId={}, userId={}", imageId, userId);

        ProductImage productImage = baseMapper.selectById(imageId);
        if (productImage == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片不存在");
        }

        // 检查商品权限
        Product product = productMapper.selectById(productImage.getProductId());
        if (product == null || !product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此图片");
        }

        // 逻辑删除
        productImage.setStatus(1);
        baseMapper.updateById(productImage);

        log.info("商品图片删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCoverImage(Long imageId, Long userId) {
        log.info("设置封面图, imageId={}, userId={}", imageId, userId);

        ProductImage productImage = baseMapper.selectById(imageId);
        if (productImage == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片不存在");
        }

        // 检查商品权限
        Product product = productMapper.selectById(productImage.getProductId());
        if (product == null || !product.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此图片");
        }

        // 取消该商品其他图片的封面状态
        LambdaUpdateWrapper<ProductImage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductImage::getProductId, productImage.getProductId())
                .set(ProductImage::getIsCover, 0);
        baseMapper.update(null, updateWrapper);

        // 设置当前图片为封面
        productImage.setIsCover(1);
        baseMapper.updateById(productImage);

        log.info("封面图设置成功");
    }

    @Override
    public List<ProductImageVO> getProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                .eq(ProductImage::getStatus, 0)
                .orderByAsc(ProductImage::getSortOrder)
                .orderByDesc(ProductImage::getIsCover);

        return baseMapper.selectList(queryWrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private ProductImageVO convertToVO(ProductImage productImage) {
        ProductImageVO vo = new ProductImageVO();
        BeanUtil.copyProperties(productImage, vo);
        if (productImage.getCreateTime() != null) {
            vo.setCreateTime(productImage.getCreateTime().toString());
        }
        return vo;
    }
}
