package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.FlashSaleActivity;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.FlashSaleSessionVO;
import com.xx.xianqijava.vo.ProductVO;

import java.util.List;

/**
 * 秒杀服务接口（优化版）
 */
public interface FlashSaleService extends IService<FlashSaleActivity> {

    /**
     * 获取当前进行中的秒杀活动
     */
    FlashSaleActivity getCurrentActivity();

    /**
     * 获取当前可用的秒杀商品（供首页展示）
     */
    List<ProductVO> getCurrentFlashSaleProducts(Integer limit);

    /**
     * 获取活动商品列表（包含秒杀价）
     */
    List<FlashSaleProductVO> getActivityProducts(Long activityId);

    /**
     * 更新活动状态（定时任务调用）
     */
    void updateActivityStatus();

    // ========== 新增方法 ==========

    /**
     * 获取当前可见的场次列表
     */
    List<FlashSaleSessionVO> getActiveSessions();

    /**
     * 获取指定场次的秒杀商品列表（分页）
     */
    List<FlashSaleProductVO> getSessionProducts(Long sessionId, Integer page, Integer pageSize);

    /**
     * 检查用户是否可以参与秒杀
     */
    boolean canBuy(Long userId, Long productId, Long activityId);

    /**
     * 获取用户在某活动中的购买数量
     */
    int getUserBuyCount(Long userId, Long activityId);

    /**
     * 获取秒杀商品详情（包含秒杀信息）
     */
    FlashSaleProductVO getFlashSaleProductDetail(Long productId);
}
