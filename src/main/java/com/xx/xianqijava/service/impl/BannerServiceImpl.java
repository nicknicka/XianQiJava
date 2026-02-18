package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.Banner;
import com.xx.xianqijava.mapper.BannerMapper;
import com.xx.xianqijava.service.BannerService;
import com.xx.xianqijava.vo.BannerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<BannerVO> getActiveBanners() {
        log.info("查询启用的轮播图列表");

        // 查询启用中的轮播图
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Banner::getStatus, 1)
                .le(Banner::getStartTime, LocalDateTime.now())
                .ge(Banner::getEndTime, LocalDateTime.now())
                .orderByAsc(Banner::getSortOrder)
                .orderByDesc(Banner::getBannerId);

        List<Banner> banners = list(queryWrapper);

        // 异步更新曝光次数（使用SQL级别更新避免并发问题）
        if (!banners.isEmpty()) {
            final java.util.List<Banner> bannersToUpdate = banners;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                for (Banner banner : bannersToUpdate) {
                    // 使用SQL更新避免并发丢失
                    com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Banner> updateWrapper =
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
                    updateWrapper.setSql("exposure_count = exposure_count + 1")
                            .eq(Banner::getBannerId, banner.getBannerId());
                    baseMapper.update(null, updateWrapper);
                }
            });
        }

        return banners.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementClickCount(Long bannerId) {
        log.info("增加轮播图点击次数, bannerId={}", bannerId);

        Banner banner = getById(bannerId);
        if (banner != null) {
            banner.setClickCount(banner.getClickCount() + 1);
            updateById(banner);
        }
    }

    /**
     * 转换为VO
     */
    private BannerVO convertToVO(Banner banner) {
        BannerVO vo = new BannerVO();
        BeanUtil.copyProperties(banner, vo);
        return vo;
    }
}
