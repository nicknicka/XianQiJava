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

        // 转换为VO并增加曝光次数
        for (Banner banner : banners) {
            banner.setExposureCount(banner.getExposureCount() + 1);
        }
        updateBatchById(banners);

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
