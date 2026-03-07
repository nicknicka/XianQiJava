package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.ShareItemManageQueryDTO;
import com.xx.xianqijava.dto.admin.ShareItemStatusUpdateDTO;
import com.xx.xianqijava.vo.admin.ShareItemManageStatistics;
import com.xx.xianqijava.vo.admin.ShareItemManageVO;

/**
 * 共享物品管理服务接口 - 管理端
 */
public interface ShareItemManageService {

    /**
     * 分页查询共享物品列表
     *
     * @param queryDTO 查询条件
     * @return 共享物品分页数据
     */
    Page<ShareItemManageVO> getShareItemList(ShareItemManageQueryDTO queryDTO);

    /**
     * 获取共享物品详情
     *
     * @param shareId 共享物品ID
     * @return 共享物品详情
     */
    ShareItemManageVO getShareItemDetail(Long shareId);

    /**
     * 更新共享物品状态
     *
     * @param updateDTO 更新DTO
     * @return 是否成功
     */
    Boolean updateShareItemStatus(ShareItemStatusUpdateDTO updateDTO);

    /**
     * 获取共享物品统计信息
     *
     * @return 统计信息
     */
    ShareItemManageStatistics getShareItemStatistics();
}
