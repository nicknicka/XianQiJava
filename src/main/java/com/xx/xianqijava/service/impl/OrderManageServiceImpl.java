package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.OrderManageQueryDTO;
import com.xx.xianqijava.dto.admin.OrderRefundProcessDTO;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.OrderManageService;
import com.xx.xianqijava.vo.admin.OrderManageStatistics;
import com.xx.xianqijava.vo.admin.OrderManageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单管理服务实现类 - 管理端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderManageServiceImpl implements OrderManageService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    public Page<OrderManageVO> getOrderList(OrderManageQueryDTO queryDTO) {
        log.info("分页查询订单列表，查询条件：{}", queryDTO);

        // 构建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        // 订单号模糊搜索
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            queryWrapper.like(Order::getOrderNo, queryDTO.getOrderNo());
        }

        // 订单状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Order::getStatus, queryDTO.getStatus());
        }

        // 买家筛选
        if (queryDTO.getBuyerId() != null) {
            queryWrapper.eq(Order::getBuyerId, queryDTO.getBuyerId());
        }

        // 卖家筛选
        if (queryDTO.getSellerId() != null) {
            queryWrapper.eq(Order::getSellerId, queryDTO.getSellerId());
        }

        // 商品标题筛选（通过子查询）
        if (StringUtils.hasText(queryDTO.getProductTitle())) {
            // 先查询符合条件的商品ID
            List<Long> matchedProductIds = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .like(Product::getTitle, queryDTO.getProductTitle())
                            .select(Product::getProductId)
            ).stream().map(Product::getProductId).collect(Collectors.toList());

            if (matchedProductIds.isEmpty()) {
                // 没有符合条件的商品，返回空页
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
            }

            queryWrapper.in(Order::getProductId, matchedProductIds);
        }

        // 排序
        if ("totalAmount".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Order::getAmount);
        } else if ("completeTime".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Order::getFinishTime);
        } else {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), Order::getCreateTime);
        }

        // 分页查询
        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Order> orderPage = orderMapper.selectPage(page, queryWrapper);

        // 转换为VO
        return convertToVOPage(orderPage, queryDTO);
    }

    @Override
    public OrderManageVO getOrderDetail(Long orderId) {
        log.info("获取订单详情，订单ID：{}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        return convertToVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean processRefund(OrderRefundProcessDTO processDTO) {
        log.info("管理员处理退款，订单ID：{}，处理结果：{}", processDTO.getOrderId(), processDTO.getResult());

        Order order = orderMapper.selectById(processDTO.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 4) {
            throw new RuntimeException("只有退款中的订单才能处理");
        }

        // 拒绝退款时必须填写原因
        if (processDTO.getResult() == 2 && !StringUtils.hasText(processDTO.getRemark())) {
            throw new RuntimeException("拒绝退款必须填写原因");
        }

        if (processDTO.getResult() == 1) {
            // 同意退款，订单状态变为已取消
            order.setStatus(3);
            log.info("管理员同意退款，订单ID：{}，状态变更为已取消", processDTO.getOrderId());

            // 恢复商品状态为在售
            Product product = productMapper.selectById(order.getProductId());
            if (product != null && product.getStatus() == 2) {
                product.setStatus(1);
                productMapper.updateById(product);
                log.info("退款成功，恢复商品在售状态，商品ID：{}", order.getProductId());
            }
        } else {
            // 拒绝退款，订单状态恢复为进行中
            order.setStatus(1);
            log.info("管理员拒绝退款，订单ID：{}，状态恢复为进行中", processDTO.getOrderId());
        }

        int result = orderMapper.updateById(order);

        log.info("管理员处理退款{}，订单ID：{}", result > 0 ? "成功" : "失败", processDTO.getOrderId());
        return result > 0;
    }

    @Override
    public OrderManageStatistics getOrderStatistics() {
        log.info("获取订单统计信息");

        OrderManageStatistics statistics = new OrderManageStatistics();

        // 总订单数
        Long totalOrders = orderMapper.selectCount(null);
        statistics.setTotalOrders(totalOrders);

        // 待确认订单数
        Long pendingConfirmCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)
        );
        statistics.setPendingConfirmCount(pendingConfirmCount);

        // 进行中订单数
        Long inProgressCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)
        );
        statistics.setInProgressCount(inProgressCount);

        // 已完成订单数
        Long completedCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)
        );
        statistics.setCompletedCount(completedCount);

        // 已取消订单数
        Long cancelledCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)
        );
        statistics.setCancelledCount(cancelledCount);

        // 退款中订单数
        Long refundingCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 4)
        );
        statistics.setRefundingCount(refundingCount);

        // 今日新增订单数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todayNewOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, todayStart)
        );
        statistics.setTodayNewOrders(todayNewOrders);

        // 本周新增订单数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long weekNewOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, weekStart)
        );
        statistics.setWeekNewOrders(weekNewOrders);

        // 本月新增订单数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        Long monthNewOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreateTime, monthStart)
        );
        statistics.setMonthNewOrders(monthNewOrders);

        // 总交易金额（已完成订单）
        List<Order> completedOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)
        );
        BigDecimal totalAmount = completedOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setTotalAmount(totalAmount);

        // 今日交易金额
        List<Order> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 2)
                        .ge(Order::getCreateTime, todayStart)
        );
        BigDecimal todayAmount = todayOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setTodayAmount(todayAmount);

        // 本周交易金额
        List<Order> weekOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 2)
                        .ge(Order::getCreateTime, weekStart)
        );
        BigDecimal weekAmount = weekOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setWeekAmount(weekAmount);

        // 本月交易金额
        List<Order> monthOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 2)
                        .ge(Order::getCreateTime, monthStart)
        );
        BigDecimal monthAmount = monthOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setMonthAmount(monthAmount);

        return statistics;
    }

    /**
     * 转换Order分页数据为OrderManageVO分页数据
     */
    private Page<OrderManageVO> convertToVOPage(Page<Order> orderPage, OrderManageQueryDTO queryDTO) {
        // 批量获取用户信息
        List<Long> userIds = orderPage.getRecords().stream()
                .flatMap(order -> List.of(order.getBuyerId(), order.getSellerId()).stream())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 批量获取商品信息
        List<Long> productIds = orderPage.getRecords().stream()
                .map(Order::getProductId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // 转换为VO
        Page<OrderManageVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderManageVO> voList = orderPage.getRecords().stream()
                .map(order -> convertToVO(order, userMap, productMap))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 转换单个Order为OrderManageVO
     */
    private OrderManageVO convertToVO(Order order) {
        User buyer = userMapper.selectById(order.getBuyerId());
        User seller = userMapper.selectById(order.getSellerId());
        Product product = productMapper.selectById(order.getProductId());

        // 使用HashMap避免NPE，并处理null值
        Map<Long, User> userMap = new HashMap<>();
        if (buyer != null) {
            userMap.put(buyer.getUserId(), buyer);
        }
        if (seller != null) {
            userMap.put(seller.getUserId(), seller);
        }

        Map<Long, Product> productMap = new HashMap<>();
        if (product != null) {
            productMap.put(product.getProductId(), product);
        }

        return convertToVO(order, userMap, productMap);
    }

    /**
     * 转换Order为OrderManageVO
     */
    private OrderManageVO convertToVO(Order order, Map<Long, User> userMap, Map<Long, Product> productMap) {
        OrderManageVO vo = new OrderManageVO();
        BeanUtils.copyProperties(order, vo);

        // 设置买家信息
        User buyer = userMap.get(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
            vo.setBuyerPhone(buyer.getPhone());
        }

        // 设置卖家信息
        User seller = userMap.get(order.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerPhone(seller.getPhone());
        }

        // 设置商品信息
        Product product = productMap.get(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductPrice(product.getPrice());
            vo.setProductImage(getCoverImage(product));
        }

        // 设置总金额
        vo.setTotalAmount(order.getAmount());

        // 设置各种时间（使用现有的字段）
        // 注意：Order实体只有createTime、updateTime、finishTime
        // 其他时间字段需要从RefundRecord等表中获取
        vo.setUpdateTime(order.getUpdateTime());
        vo.setFinishTime(order.getFinishTime());
        // vo.setRefundStatus(0); // TODO: 从RefundRecord表获取退款状态

        return vo;
    }

    /**
     * 获取商品封面图
     */
    private String getCoverImage(Product product) {
        // TODO: 从ProductImage表获取封面图
        return "/images/default-product.png";
    }
}
