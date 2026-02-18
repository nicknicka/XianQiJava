package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.OrderCreateDTO;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.OrderService;
import com.xx.xianqijava.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    private static final AtomicInteger ORDER_COUNTER = new AtomicInteger(0);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(OrderCreateDTO createDTO, Long buyerId) {
        log.info("创建订单, buyerId={}, productId={}", buyerId, createDTO.getProductId());

        // 1. 查询商品
        Product product = productMapper.selectById(createDTO.getProductId());
        if (product == null || product.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 2. 检查商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不在售，无法下单");
        }

        // 3. 检查是否是自己的商品
        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能购买自己的商品");
        }

        // 4. 计算金额 (支持数量购买)
        BigDecimal amount = product.getPrice();
        if (createDTO.getQuantity() != null && createDTO.getQuantity() > 1) {
            amount = amount.multiply(BigDecimal.valueOf(createDTO.getQuantity()));
        }

        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductId(createDTO.getProductId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setType(1); // 1-购买
        order.setAmount(amount);
        order.setStatus(0); // 待确认
        order.setRemark(createDTO.getRemark());

        save(order);

        log.info("订单创建成功, orderId={}, orderNo={}", order.getOrderId(), order.getOrderNo());

        return convertToVO(order, createDTO.getQuantity() != null ? createDTO.getQuantity() : 1);
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限：只有买家和卖家可以查看订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该订单");
        }

        return convertToVO(order, 1);
    }

    @Override
    public IPage<OrderVO> getOrderList(Page<Order> page, Long userId, Integer role, Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // role: 0-买家, 1-卖家
        if (role == 0) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if (role == 1) {
            wrapper.eq(Order::getSellerId, userId);
        }

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = page(page, wrapper);
        return orderPage.convert(order -> convertToVO(order, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long orderId, Long sellerId) {
        log.info("确认订单, orderId={}, sellerId={}", orderId, sellerId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限：只有卖家可以确认订单
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家可以确认订单");
        }

        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确，无法确认");
        }

        // 更新订单状态
        order.setStatus(1); // 进行中
        updateById(order);

        log.info("订单确认成功, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        log.info("取消订单, orderId={}, userId={}", orderId, userId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限：只有买家和卖家可以取消订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权取消该订单");
        }

        // 检查订单状态
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确，无法取消");
        }

        // 更新订单状态
        order.setStatus(3); // 已取消
        updateById(order);

        log.info("订单取消成功, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long userId) {
        log.info("完成订单, orderId={}, userId={}", orderId, userId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限：只有买家可以完成订单
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以完成订单");
        }

        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确，无法完成");
        }

        // 更新订单状态
        order.setStatus(2); // 已完成
        order.setFinishTime(LocalDateTime.now());
        updateById(order);

        // 更新商品状态为已售
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus(2); // 已售
            productMapper.updateById(product);
        }

        log.info("订单完成成功, orderId={}", orderId);
    }

    @Override
    public String generateOrderNo() {
        // 生成格式：yyyyMMddHHmmss + 4位随机数
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ORDER_COUNTER.incrementAndGet() % 10000;
        return datetime + String.format("%04d", random);
    }

    /**
     * 转换为VO
     */
    private OrderVO convertToVO(Order order, Integer quantity) {
        OrderVO vo = new OrderVO();
        BeanUtil.copyProperties(order, vo);

        // 查询商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            // TODO: 从 product_image 表获取第一张图片
            // 计算单价
            vo.setUnitPrice(product.getPrice());
        }

        // 设置数量
        vo.setQuantity(quantity);
        vo.setTotalPrice(order.getAmount());

        // 查询买家信息
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
            vo.setBuyerAvatar(buyer.getAvatar());
        }

        // 查询卖家信息
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(order.getStatus()));

        // 设置完成时间
        vo.setCompleteTime(order.getFinishTime());

        return vo;
    }

    /**
     * 获取订单状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待确认";
            case 1:
                return "进行中";
            case 2:
                return "已完成";
            case 3:
                return "已取消";
            case 4:
                return "已退款";
            default:
                return "未知状态";
        }
    }
}
