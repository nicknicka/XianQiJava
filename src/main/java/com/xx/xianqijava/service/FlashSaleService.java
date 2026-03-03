package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.FlashSaleSessionVO;
import com.xx.xianqijava.vo.ProductVO;

import java.util.List;

/**
 * 秒杀服务接口（已简化，只使用场次）
 */
public interface FlashSaleService extends IService<FlashSaleSession> {

    /**
     * 获取当前进行中的秒杀场次
     */
    FlashSaleSession getCurrentSession();

    /**
     * 获取当前可用的秒杀商品（供首页展示）
     */
    List<ProductVO> getCurrentFlashSaleProducts(Integer limit);

    /**
     * 获取指定场次的商品列表（包含秒杀价）
     */
    List<FlashSaleProductVO> getSessionProducts(Long sessionId);

    /**
     * 更新场次状态（定时任务调用）
     */
    void updateSessionStatus();

    // ========== 场次相关方法 ==========

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
    boolean canBuy(Long userId, Long productId, Long sessionId);

    /**
     * 获取用户在某场次中的购买数量
     */
    int getUserBuyCount(Long userId, Long sessionId);

    /**
     * 获取秒杀商品详情（包含秒杀信息）
     */
    FlashSaleProductVO getFlashSaleProductDetail(Long productId);

    // ========== 前端秒杀页面所需方法 ==========

    /**
     * 检查秒杀购买资格
     */
    boolean checkSeckillEligibility(Long userId, Long productId);

    /**
     * 获取无法购买的原因
     */
    String getCannotBuyReason(Long userId, Long productId);

    /**
     * 获取剩余库存
     */
    Integer getRemainingStock(Long productId);

    /**
     * 获取用户限购数量
     */
    Integer getUserBuyLimit(Long productId);

    /**
     * 秒杀抢购
     */
    java.util.Map<String, Object> seckillBuy(Long userId, Long productId, Integer quantity, String remark);
}
