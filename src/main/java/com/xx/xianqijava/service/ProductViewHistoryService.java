package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.ProductViewHistory;
import com.xx.xianqijava.vo.ProductVO;

/**
 * 商品浏览历史服务接口
 */
public interface ProductViewHistoryService extends IService<ProductViewHistory> {

    /**
     * 记录浏览历史
     *
     * @param userId    用户ID
     * @param productId 商品ID
     */
    void recordViewHistory(Long userId, Long productId);

    /**
     * 获取浏览历史列表（分页）
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 浏览历史列表
     */
    IPage<ProductVO> getViewHistoryList(Long userId, Page<ProductViewHistory> page);

    /**
     * 删除浏览记录
     *
     * @param userId    用户ID
     * @param historyId 浏览记录ID
     */
    void removeViewHistory(Long userId, Long historyId);

    /**
     * 清空浏览历史
     *
     * @param userId 用户ID
     */
    void clearViewHistory(Long userId);
}
