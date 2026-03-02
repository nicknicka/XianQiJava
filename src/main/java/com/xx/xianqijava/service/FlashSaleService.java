package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.FlashSaleActivity;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.ProductVO;

import java.util.List;

/**
 * 秒杀服务接口
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
}
