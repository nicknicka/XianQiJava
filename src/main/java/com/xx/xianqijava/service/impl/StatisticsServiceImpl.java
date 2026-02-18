package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.entity.*;
import com.xx.xianqijava.mapper.*;
import com.xx.xianqijava.service.StatisticsService;
import com.xx.xianqijava.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final EvaluationMapper evaluationMapper;
    private final CategoryMapper categoryMapper;
    private final UserVerificationMapper userVerificationMapper;
    private final SystemNotificationMapper systemNotificationMapper;
    private final UserFeedbackMapper userFeedbackMapper;
    private final ReportMapper reportMapper;

    @Override
    public StatisticsVO getOverviewStatistics() {
        log.info("获取总览统计数据");

        StatisticsVO vo = new StatisticsVO();

        // 用户统计
        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTodayNewUsers(countUsersCreatedAfter(LocalDateTime.now().with(LocalTime.MIN)));
        vo.setActiveUsers(countActiveUsers());

        // 商品统计
        vo.setTotalProducts(productMapper.selectCount(null));
        vo.setOnSaleProducts(countProductsByStatus(1).intValue());
        vo.setTodayNewProducts(countProductsCreatedAfter(LocalDateTime.now().with(LocalTime.MIN)));

        // 订单统计
        vo.setTotalOrders(countOrders());
        vo.setTodayNewOrders(countOrdersCreatedAfter(LocalDateTime.now().with(LocalTime.MIN)));
        vo.setPendingOrders(countOrdersByStatus(0));
        vo.setCompletedOrders(countOrdersByStatus(2)); // 2-已完成

        // 交易金额
        vo.setTotalAmount(calculateTotalAmount());
        vo.setTodayAmount(calculateAmountAfter(LocalDateTime.now().with(LocalTime.MIN)));
        vo.setMonthAmount(calculateAmountAfter(LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN)));

        // 待处理事项
        vo.setPendingProducts(countProductsByAuditStatus(0));
        vo.setPendingVerifications(countVerificationsByStatus(0));
        vo.setSystemNotifications(countNotificationsAfter(LocalDateTime.now().minusDays(7)));
        vo.setUserFeedbacks(countFeedbacksByStatus(0));
        vo.setPendingReports(countReportsByStatus(0));

        return vo;
    }

    @Override
    public UserStatisticsVO getUserStatistics() {
        log.info("获取用户统计数据");

        UserStatisticsVO vo = new UserStatisticsVO();

        // 基础统计
        vo.setTotalUsers(userMapper.selectCount(null));
        vo.setTodayNewUsers(countUsersCreatedAfter(LocalDateTime.now().with(LocalTime.MIN)));
        vo.setWeekNewUsers(countUsersCreatedAfter(LocalDateTime.now().minusDays(7)));
        vo.setMonthNewUsers(countUsersCreatedAfter(LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN)));
        vo.setActiveUsers(countActiveUsers());
        vo.setVerifiedUsers(countUsersByVerificationStatus(1));
        vo.setBannedUsers(countUsersByStatus(1));

        // 注册趋势（最近30天）
        vo.setRegisterTrend(getUserRegisterTrend(30));

        return vo;
    }

    @Override
    public ProductStatisticsVO getProductStatistics() {
        log.info("获取商品统计数据");

        ProductStatisticsVO vo = new ProductStatisticsVO();

        // 基础统计
        vo.setTotalProducts(productMapper.selectCount(null));
        vo.setOnSaleProducts(countProductsByStatus(1));
        vo.setSoldProducts(countProductsByStatus(2));
        vo.setOfflineProducts(countProductsByStatus(0));
        vo.setTodayNewProducts(countProductsCreatedAfter(LocalDateTime.now().with(LocalTime.MIN)));
        vo.setWeekNewProducts(countProductsCreatedAfter(LocalDateTime.now().minusDays(7)));
        vo.setMonthNewProducts(countProductsCreatedAfter(LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN)));

        // 审核统计
        vo.setPendingProducts(countProductsByAuditStatus(0));
        vo.setApprovedProducts(countProductsByAuditStatus(1));
        vo.setRejectedProducts(countProductsByAuditStatus(2));

        // 发布趋势（最近30天）
        vo.setPublishTrend(getProductPublishTrend(30));

        // 按分类统计
        vo.setCategoryStatistics(getCategoryStatistics());

        return vo;
    }

    @Override
    public OrderStatisticsVO getOrderStatistics() {
        log.info("获取订单统计数据");

        OrderStatisticsVO vo = new OrderStatisticsVO();

        // 基础统计
        vo.setTotalOrders(countOrders());
        vo.setPendingOrders(countOrdersByStatus(0));      // 0-待确认
        vo.setInProgressOrders(countOrdersByStatus(1));   // 1-进行中
        vo.setCompletedOrders(countOrdersByStatus(2));    // 2-已完成
        vo.setCancelledOrders(countOrdersByStatus(3));    // 3-已取消
        vo.setRefundingOrders(countOrdersByStatus(4));    // 4-退款中

        // 新增统计
        vo.setTodayNewOrders(countOrdersCreatedAfter(LocalDateTime.now().with(LocalTime.MIN)));
        vo.setWeekNewOrders(countOrdersCreatedAfter(LocalDateTime.now().minusDays(7)));
        vo.setMonthNewOrders(countOrdersCreatedAfter(LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN)));

        // 金额统计
        vo.setTotalAmount(calculateTotalAmount());
        vo.setTodayAmount(calculateAmountAfter(LocalDateTime.now().with(LocalTime.MIN)));
        vo.setWeekAmount(calculateAmountAfter(LocalDateTime.now().minusDays(7)));
        vo.setMonthAmount(calculateAmountAfter(LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN)));

        // 趋势（最近30天）
        vo.setOrderTrend(getOrderTrend(30));
        vo.setAmountTrend(getAmountTrend(30));

        return vo;
    }

    // ==================== 私有辅助方法 ====================

    private Long countUsersCreatedAfter(LocalDateTime dateTime) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getCreateTime, dateTime);
        return userMapper.selectCount(wrapper);
    }

    private Long countActiveUsers() {
        // 7天内登录过的用户（假设最后更新时间在7天内）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getUpdateTime, LocalDateTime.now().minusDays(7));
        return userMapper.selectCount(wrapper);
    }

    private Long countUsersByVerificationStatus(Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsVerified, status);
        return userMapper.selectCount(wrapper);
    }

    private Long countUsersByStatus(Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, status);
        return userMapper.selectCount(wrapper);
    }

    private Long countProducts() {
        return productMapper.selectCount(null);
    }

    private Long countProductsByStatus(Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, status);
        return productMapper.selectCount(wrapper);
    }

    private Long countProductsByAuditStatus(Integer auditStatus) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getAuditStatus, auditStatus);
        return productMapper.selectCount(wrapper);
    }

    private Long countProductsCreatedAfter(LocalDateTime dateTime) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Product::getCreateTime, dateTime);
        return productMapper.selectCount(wrapper);
    }

    private Long countOrders() {
        return orderMapper.selectCount(null);
    }

    private Long countOrdersByStatus(Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, status);
        return orderMapper.selectCount(wrapper);
    }

    private Long countOrdersCreatedAfter(LocalDateTime dateTime) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreateTime, dateTime);
        return orderMapper.selectCount(wrapper);
    }

    private BigDecimal calculateTotalAmount() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 2); // 只统计已完成的订单
        wrapper.select(Order::getAmount);

        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAmountAfter(LocalDateTime dateTime) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreateTime, dateTime);
        wrapper.eq(Order::getStatus, 2); // 只统计已完成的订单
        wrapper.select(Order::getAmount);

        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long countVerificationsByStatus(Integer status) {
        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVerification::getStatus, status);
        return userVerificationMapper.selectCount(wrapper);
    }

    private Long countNotificationsAfter(LocalDateTime dateTime) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SystemNotification::getCreateTime, dateTime);
        return systemNotificationMapper.selectCount(wrapper);
    }

    private Long countFeedbacksByStatus(Integer status) {
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFeedback::getStatus, status);
        return userFeedbackMapper.selectCount(wrapper);
    }

    private Long countReportsByStatus(Integer status) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, status);
        return reportMapper.selectCount(wrapper);
    }

    /**
     * 获取用户注册趋势
     */
    private List<TrendDataVO> getUserRegisterTrend(int days) {
        List<TrendDataVO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(User::getCreateTime, startOfDay);
            wrapper.le(User::getCreateTime, endOfDay);

            long count = userMapper.selectCount(wrapper);

            TrendDataVO data = new TrendDataVO();
            data.setDate(date.getMonthValue() + "-" + date.getDayOfMonth()); // MM-dd
            data.setCount(count);
            trend.add(data);
        }

        return trend;
    }

    /**
     * 获取商品发布趋势
     */
    private List<TrendDataVO> getProductPublishTrend(int days) {
        List<TrendDataVO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Product::getCreateTime, startOfDay);
            wrapper.le(Product::getCreateTime, endOfDay);

            long count = productMapper.selectCount(wrapper);

            TrendDataVO data = new TrendDataVO();
            data.setDate(date.getMonthValue() + "-" + date.getDayOfMonth()); // MM-dd
            data.setCount(count);
            trend.add(data);
        }

        return trend;
    }

    /**
     * 获取订单趋势
     */
    private List<TrendDataVO> getOrderTrend(int days) {
        List<TrendDataVO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Order::getCreateTime, startOfDay);
            wrapper.le(Order::getCreateTime, endOfDay);

            long count = orderMapper.selectCount(wrapper);

            TrendDataVO data = new TrendDataVO();
            data.setDate(date.getMonthValue() + "-" + date.getDayOfMonth()); // MM-dd
            data.setCount(count);
            trend.add(data);
        }

        return trend;
    }

    /**
     * 获取交易金额趋势
     */
    private List<TrendDataVO> getAmountTrend(int days) {
        List<TrendDataVO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Order::getCreateTime, startOfDay);
            wrapper.le(Order::getCreateTime, endOfDay);
            wrapper.eq(Order::getStatus, 2); // 只统计已完成的订单

            List<Order> orders = orderMapper.selectList(wrapper);
            BigDecimal total = orders.stream()
                    .map(Order::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            TrendDataVO data = new TrendDataVO();
            data.setDate(date.getMonthValue() + "-" + date.getDayOfMonth()); // MM-dd
            data.setCount(total.longValue()); // 金额转为long
            trend.add(data);
        }

        return trend;
    }

    /**
     * 按分类统计商品数量
     */
    private List<CategoryStatisticsVO> getCategoryStatistics() {
        // 获取所有分类
        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(Category::getStatus, 1); // 只统计启用的分类
        List<Category> categories = categoryMapper.selectList(categoryWrapper);

        return categories.stream()
                .map(category -> {
                    CategoryStatisticsVO vo = new CategoryStatisticsVO();
                    vo.setCategoryId(category.getCategoryId());
                    vo.setCategoryName(category.getName());

                    LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
                    productWrapper.eq(Product::getCategoryId, category.getCategoryId());
                    Long count = productMapper.selectCount(productWrapper);
                    vo.setProductCount(count);

                    return vo;
                })
                .sorted((a, b) -> Long.compare(b.getProductCount(), a.getProductCount())) // 按数量降序
                .collect(Collectors.toList());
    }
}
