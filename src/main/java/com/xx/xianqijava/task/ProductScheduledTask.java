package com.xx.xianqijava.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.enums.ProductStatus;
import com.xx.xianqijava.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 商品定时任务
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductScheduledTask {

    private final ProductMapper productMapper;

    /**
     * 自动下架到期商品
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoExpireProducts() {
        log.info("开始执行自动下架到期商品任务");

        try {
            // 商品发布超过30天自动下架
            LocalDateTime expireTime = LocalDateTime.now().minusDays(30);

            LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Product::getStatus, ProductStatus.ON_SALE.getCode())
                    .le(Product::getCreateTime, expireTime)
                    .set(Product::getStatus, ProductStatus.EXPIRED.getCode())
                    .set(Product::getUpdateTime, LocalDateTime.now());

            int updatedCount = productMapper.update(null, updateWrapper);

            log.info("自动下架到期商品任务完成，下架商品数：{}", updatedCount);
        } catch (Exception e) {
            log.error("自动下架到期商品任务执行失败", e);
        }
    }

    /**
     * 自动下售完商品（库存为0且一定时间未补充）
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoSoldOutProducts() {
        log.info("开始执行自动下架售完商品任务");

        try {
            // 将状态为在售但库存为0的商品标记为已售
            LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Product::getStatus, ProductStatus.ON_SALE.getCode())
                    .eq(Product::getStock, 0)
                    .set(Product::getStatus, ProductStatus.SOLD_OUT.getCode())
                    .set(Product::getUpdateTime, LocalDateTime.now());

            int updatedCount = productMapper.update(null, updateWrapper);

            log.info("自动下架售完商品任务完成，下架商品数：{}", updatedCount);
        } catch (Exception e) {
            log.error("自动下架售完商品任务执行失败", e);
        }
    }
}
