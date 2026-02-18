package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductViewHistory;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.ProductViewHistoryMapper;
import com.xx.xianqijava.service.ProductService;
import com.xx.xianqijava.service.ProductViewHistoryService;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 商品浏览历史服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductViewHistoryServiceImpl extends ServiceImpl<ProductViewHistoryMapper, ProductViewHistory>
        implements ProductViewHistoryService {

    private final ProductMapper productMapper;
    private final ProductService productService;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void recordViewHistory(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return;
        }

        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return;
        }

        // 检查是否已有浏览记录
        LambdaQueryWrapper<ProductViewHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductViewHistory::getUserId, userId)
                .eq(ProductViewHistory::getProductId, productId);
        ProductViewHistory existHistory = baseMapper.selectOne(queryWrapper);

        if (existHistory != null) {
            // 更新浏览时间
            existHistory.setViewTime(LocalDateTime.now());
            baseMapper.updateById(existHistory);
        } else {
            // 新增浏览记录
            ProductViewHistory history = new ProductViewHistory();
            history.setUserId(userId);
            history.setProductId(productId);
            history.setViewTime(LocalDateTime.now());
            history.setViewDuration(0);
            baseMapper.insert(history);
        }
    }

    @Override
    public IPage<ProductVO> getViewHistoryList(Long userId, Page<ProductViewHistory> page) {
        // 查询浏览历史记录
        IPage<ProductViewHistory> historyPage = baseMapper.selectPage(page,
                new LambdaQueryWrapper<ProductViewHistory>()
                        .eq(ProductViewHistory::getUserId, userId)
                        .orderByDesc(ProductViewHistory::getViewTime));

        // 转换为ProductVO
        return historyPage.convert(history -> {
            Product product = productMapper.selectById(history.getProductId());
            if (product == null) {
                return null;
            }
            return productService.convertToVO(product, userId);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeViewHistory(Long userId, Long historyId) {
        LambdaQueryWrapper<ProductViewHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductViewHistory::getUserId, userId)
                .eq(ProductViewHistory::getHistoryId, historyId);
        baseMapper.delete(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearViewHistory(Long userId) {
        LambdaQueryWrapper<ProductViewHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductViewHistory::getUserId, userId);
        baseMapper.delete(queryWrapper);
    }
}
