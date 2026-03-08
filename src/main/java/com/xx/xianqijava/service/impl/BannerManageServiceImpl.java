package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.BannerCreateDTO;
import com.xx.xianqijava.dto.admin.BannerManageQueryDTO;
import com.xx.xianqijava.dto.admin.BannerUpdateDTO;
import com.xx.xianqijava.entity.Banner;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.mapper.BannerMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.service.BannerManageService;
import com.xx.xianqijava.vo.admin.BannerManageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图管理服务实现类 - 管理端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerManageServiceImpl implements BannerManageService {

    private final BannerMapper bannerMapper;
    private final ProductMapper productMapper;

    @Override
    public Page<BannerManageVO> getBannerList(BannerManageQueryDTO queryDTO) {
        log.info("分页查询轮播图列表，查询条件：{}", queryDTO);

        // 构建查询条件
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();

        // 标题模糊搜索
        if (StringUtils.hasText(queryDTO.getTitle())) {
            queryWrapper.like(Banner::getTitle, queryDTO.getTitle());
        }

        // 链接类型筛选
        if (queryDTO.getLinkType() != null) {
            queryWrapper.eq(Banner::getLinkType, queryDTO.getLinkType());
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Banner::getStatus, queryDTO.getStatus());
        }

        // 排序
        if ("clickCount".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Banner::getClickCount);
        } else if ("exposureCount".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Banner::getExposureCount);
        } else if ("createTime".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Banner::getCreateTime);
        } else {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Banner::getSortOrder);
        }

        // 分页查询
        Page<Banner> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Banner> bannerPage = bannerMapper.selectPage(page, queryWrapper);

        // 转换为VO
        return convertToVOPage(bannerPage);
    }

    @Override
    public BannerManageVO getBannerDetail(Long bannerId) {
        log.info("获取轮播图详情，轮播图ID：{}", bannerId);

        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) {
            throw new RuntimeException("轮播图不存在");
        }

        return convertToVO(banner);
    }

    @Override
    public Boolean createBanner(BannerCreateDTO createDTO) {
        log.info("创建轮播图，标题：{}", createDTO.getTitle());

        Banner banner = new Banner();
        BeanUtils.copyProperties(createDTO, banner);

        // 设置默认值
        if (banner.getClickCount() == null) {
            banner.setClickCount(0);
        }
        if (banner.getExposureCount() == null) {
            banner.setExposureCount(0);
        }

        int result = bannerMapper.insert(banner);

        log.info("创建轮播图{}，轮播图ID：{}", result > 0 ? "成功" : "失败", banner.getBannerId());
        return result > 0;
    }

    @Override
    public Boolean updateBanner(BannerUpdateDTO updateDTO) {
        log.info("更新轮播图，轮播图ID：{}", updateDTO.getBannerId());

        Banner banner = bannerMapper.selectById(updateDTO.getBannerId());
        if (banner == null) {
            throw new RuntimeException("轮播图不存在");
        }

        // 只更新非空字段
        if (StringUtils.hasText(updateDTO.getTitle())) {
            banner.setTitle(updateDTO.getTitle());
        }
        if (StringUtils.hasText(updateDTO.getImageUrl())) {
            banner.setImageUrl(updateDTO.getImageUrl());
        }
        if (StringUtils.hasText(updateDTO.getImageThumbnailUrl())) {
            banner.setImageThumbnailUrl(updateDTO.getImageThumbnailUrl());
        }
        if (updateDTO.getLinkType() != null) {
            banner.setLinkType(updateDTO.getLinkType());
        }
        if (updateDTO.getLinkUrl() != null) {
            banner.setLinkUrl(updateDTO.getLinkUrl());
        }
        if (updateDTO.getLinkProductId() != null) {
            banner.setLinkProductId(updateDTO.getLinkProductId());
        }
        if (updateDTO.getLinkPagePath() != null) {
            banner.setLinkPagePath(updateDTO.getLinkPagePath());
        }
        if (updateDTO.getSortOrder() != null) {
            banner.setSortOrder(updateDTO.getSortOrder());
        }
        if (updateDTO.getStatus() != null) {
            banner.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getStartTime() != null) {
            banner.setStartTime(updateDTO.getStartTime());
        }
        if (updateDTO.getEndTime() != null) {
            banner.setEndTime(updateDTO.getEndTime());
        }

        int result = bannerMapper.updateById(banner);

        log.info("更新轮播图{}，轮播图ID：{}", result > 0 ? "成功" : "失败", updateDTO.getBannerId());
        return result > 0;
    }

    @Override
    public Boolean deleteBanner(Long bannerId) {
        log.info("删除轮播图，轮播图ID：{}", bannerId);

        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) {
            throw new RuntimeException("轮播图不存在");
        }

        int result = bannerMapper.deleteById(bannerId);

        log.info("删除轮播图{}，轮播图ID：{}", result > 0 ? "成功" : "失败", bannerId);
        return result > 0;
    }

    @Override
    public Boolean updateBannerStatus(Long bannerId, Integer status) {
        log.info("更新轮播图状态，轮播图ID：{}，状态：{}", bannerId, status);

        Banner banner = bannerMapper.selectById(bannerId);
        if (banner == null) {
            throw new RuntimeException("轮播图不存在");
        }

        banner.setStatus(status);
        int result = bannerMapper.updateById(banner);

        log.info("更新轮播图状态{}，轮播图ID：{}", result > 0 ? "成功" : "失败", bannerId);
        return result > 0;
    }

    /**
     * 转换Banner分页数据为BannerManageVO分页数据
     */
    private Page<BannerManageVO> convertToVOPage(Page<Banner> bannerPage) {
        // 批量获取关联商品信息
        List<Long> productIds = bannerPage.getRecords().stream()
                .filter(b -> b.getLinkProductId() != null)
                .map(Banner::getLinkProductId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, Product> productMap;
        if (!productIds.isEmpty()) {
            productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getProductId, p -> p));
        } else {
            productMap = new HashMap<>();
        }

        // 转换为VO
        Page<BannerManageVO> voPage = new Page<>(bannerPage.getCurrent(), bannerPage.getSize(), bannerPage.getTotal());
        List<BannerManageVO> voList = bannerPage.getRecords().stream()
                .map(banner -> convertToVO(banner, productMap))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 转换单个Banner为BannerManageVO
     */
    private BannerManageVO convertToVO(Banner banner) {
        Map<Long, Product> productMap = new HashMap<>();
        if (banner.getLinkProductId() != null) {
            Product product = productMapper.selectById(banner.getLinkProductId());
            if (product != null) {
                productMap.put(product.getProductId(), product);
            }
        }

        return convertToVO(banner, productMap);
    }

    /**
     * 转换Banner为BannerManageVO
     */
    private BannerManageVO convertToVO(Banner banner, Map<Long, Product> productMap) {
        BannerManageVO vo = new BannerManageVO();
        BeanUtils.copyProperties(banner, vo);

        // 设置关联商品标题
        if (banner.getLinkProductId() != null) {
            Product product = productMap.get(banner.getLinkProductId());
            if (product != null) {
                vo.setLinkProductTitle(product.getTitle());
            }
        }

        return vo;
    }
}
