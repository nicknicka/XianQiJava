package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.ShareItemImage;

/**
 * 共享物品图片服务接口
 *
 * @author Claude Code
 * @since 2026-03-08
 */
public interface ShareItemImageService extends IService<ShareItemImage> {

    /**
     * 获取共享物品的封面图URL
     *
     * @param shareId 共享物品ID
     * @return 封面图URL，如果没有则返回null
     */
    String getCoverImage(Long shareId);

    /**
     * 获取共享物品的第一张图片URL
     *
     * @param shareId 共享物品ID
     * @return 第一张图片URL，如果没有则返回null
     */
    String getFirstImage(Long shareId);

    /**
     * 获取共享物品的封面图实体
     *
     * @param shareId 共享物品ID
     * @return 封面图实体，如果没有则返回null
     */
    ShareItemImage getCoverImageEntity(Long shareId);

    /**
     * 获取共享物品的第一张图片实体
     *
     * @param shareId 共享物品ID
     * @return 第一张图片实体，如果没有则返回null
     */
    ShareItemImage getFirstImageEntity(Long shareId);
}
