package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.ShareItemCreateDTO;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.vo.ShareItemVO;

/**
 * 共享物品服务接口
 */
public interface ShareItemService extends IService<ShareItem> {

    /**
     * 创建共享物品
     *
     * @param createDTO 共享物品信息
     * @param ownerId   所有者ID
     * @return 共享物品VO
     */
    ShareItemVO createShareItem(ShareItemCreateDTO createDTO, Long ownerId);

    /**
     * 更新共享物品
     *
     * @param shareId   共享物品ID
     * @param createDTO 共享物品信息
     * @param ownerId   当前用户ID
     * @return 共享物品VO
     */
    ShareItemVO updateShareItem(Long shareId, ShareItemCreateDTO createDTO, Long ownerId);

    /**
     * 删除共享物品
     *
     * @param shareId 共享物品ID
     * @param ownerId 当前用户ID
     */
    void deleteShareItem(Long shareId, Long ownerId);

    /**
     * 更新共享物品状态
     *
     * @param shareId 共享物品ID
     * @param status  状态
     * @param ownerId 当前用户ID
     */
    void updateShareItemStatus(Long shareId, Integer status, Long ownerId);

    /**
     * 获取共享物品详情
     *
     * @param shareId 共享物品ID
     * @return 共享物品VO
     */
    ShareItemVO getShareItemDetail(Long shareId);

    /**
     * 获取共享物品列表（分页）
     *
     * @param page      分页参数
     * @param categoryId 分类ID（可选）
     * @param status    状态（可选）
     * @param keyword   搜索关键词（可选）
     * @return 共享物品列表
     */
    IPage<ShareItemVO> getShareItemList(Page<ShareItem> page, Long categoryId, Integer status, String keyword);

    /**
     * 获取我的共享物品列表
     *
     * @param page    分页参数
     * @param ownerId 所有者ID
     * @return 共享物品列表
     */
    IPage<ShareItemVO> getMyShareItems(Page<ShareItem> page, Long ownerId);

    /**
     * 获取附近的共享物品列表
     *
     * @param page     分页参数
     * @param userId   当前用户ID
     * @return 共享物品列表
     */
    IPage<ShareItemVO> getNearbyShareItems(Page<ShareItem> page, Long userId);
}
