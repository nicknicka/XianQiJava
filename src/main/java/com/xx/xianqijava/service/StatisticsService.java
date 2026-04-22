package com.xx.xianqijava.service;

import java.time.LocalDate;

import com.xx.xianqijava.vo.OrderStatisticsVO;
import com.xx.xianqijava.vo.ProductStatisticsVO;
import com.xx.xianqijava.vo.StatisticsVO;
import com.xx.xianqijava.vo.UserStatisticsVO;

/**
 * 数据统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取总览统计数据
     *
     * @return 统计数据VO
     */
    StatisticsVO getOverviewStatistics();

    /**
     * 获取用户统计数据
     *
     * @return 用户统计VO
     */
    UserStatisticsVO getUserStatistics();

    /**
     * 获取商品统计数据
     *
     * @return 商品统计VO
     */
    ProductStatisticsVO getProductStatistics();

    /**
     * 获取订单统计数据
     *
     * @return 订单统计VO
     */
    OrderStatisticsVO getOrderStatistics();

    /**
     * 生成并缓存指定日期的统计数据
     *
     * @param date 统计日期
     */
    void generateDailyStatistics(LocalDate date);
}
