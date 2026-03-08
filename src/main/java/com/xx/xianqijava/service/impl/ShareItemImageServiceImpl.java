package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.ShareItemImage;
import com.xx.xianqijava.mapper.ShareItemImageMapper;
import com.xx.xianqijava.service.ShareItemImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 共享物品图片服务实现类
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareItemImageServiceImpl extends ServiceImpl<ShareItemImageMapper, ShareItemImage>
        implements ShareItemImageService {

    @Override
    public String getCoverImage(Long shareId) {
        ShareItemImage coverImage = getCoverImageEntity(shareId);
        if (coverImage == null) {
            return null;
        }
        return buildImageUrl(coverImage);
    }

    @Override
    public String getFirstImage(Long shareId) {
        ShareItemImage firstImage = getFirstImageEntity(shareId);
        if (firstImage == null) {
            return null;
        }
        return buildImageUrl(firstImage);
    }

    @Override
    public ShareItemImage getCoverImageEntity(Long shareId) {
        LambdaQueryWrapper<ShareItemImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShareItemImage::getShareId, shareId)
                .eq(ShareItemImage::getStatus, 0)
                .eq(ShareItemImage::getIsCover, 1)
                .last("LIMIT 1");

        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public ShareItemImage getFirstImageEntity(Long shareId) {
        // 先尝试获取封面图
        ShareItemImage coverImage = getCoverImageEntity(shareId);
        if (coverImage != null) {
            return coverImage;
        }

        // 如果没有封面图，获取排序第一的图片
        LambdaQueryWrapper<ShareItemImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShareItemImage::getShareId, shareId)
                .eq(ShareItemImage::getStatus, 0)
                .orderByAsc(ShareItemImage::getSortOrder)
                .last("LIMIT 1");

        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 构建图片URL
     *
     * @param shareItemImage 共享物品图片实体
     * @return 图片URL
     */
    private String buildImageUrl(ShareItemImage shareItemImage) {
        if (shareItemImage == null) {
            return null;
        }

        // 直接返回图片URL
        return shareItemImage.getImageUrl();
    }
}
