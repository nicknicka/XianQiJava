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
import com.xx.xianqijava.service.OperationLogService;
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
    private final OperationLogService operationLogService;

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

        // 4. 验证数量（防止恶意下单）
        int quantity = createDTO.getQuantity() != null && createDTO.getQuantity() > 0
                ? createDTO.getQuantity() : 1;
        if (quantity > 10) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单次购买数量不能超过10件");
        }

        // 5. 计算金额
        BigDecimal amount = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        // 6. 先将商品状态改为已售（防止超卖）
        product.setStatus(2); // 已售状态
        int updated = productMapper.updateById(product);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品已被其他买家下单，请稍后重试");
        }

        // 7. 创建订单
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

        // 记录操作日志
        User buyer = userMapper.selectById(buyerId);
        if (buyer != null) {
            operationLogService.recordLog(
                    buyerId,
                    buyer.getNickname(),
                    "订单",
                    "创建订单",
                    "创建订单 " + order.getOrderNo(),
                    "POST",
                    "/order",
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    order.getOrderId(),
                    "order"
            );
        }

        log.info("订单创建成功, orderId={}, orderNo={}", order.getOrderId(), order.getOrderNo());

        return convertToVO(order, quantity);
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

        // 记录操作日志
        User seller = userMapper.selectById(sellerId);
        if (seller != null) {
            operationLogService.recordLog(
                    sellerId,
                    seller.getNickname(),
                    "订单",
                    "确认订单",
                    "确认订单 " + order.getOrderNo(),
                    "PUT",
                    "/order/" + orderId + "/confirm",
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    orderId,
                    "order"
            );
        }

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

        // 保存原始状态用于判断是否需要恢复商品
        Integer originalStatus = order.getStatus();

        // 更新订单状态
        order.setStatus(3); // 已取消
        updateById(order);

        // 记录操作日志
        User user = userMapper.selectById(userId);
        if (user != null) {
            String role = order.getBuyerId().equals(userId) ? "买家" : "卖家";
            operationLogService.recordLog(
                    userId,
                    user.getNickname(),
                    "订单",
                    "取消订单",
                    role + "取消订单 " + order.getOrderNo(),
                    "PUT",
                    "/order/" + orderId + "/cancel",
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    orderId,
                    "order"
            );
        }

        // 如果是待确认状态取消，恢复商品状态为在售
        if (originalStatus == 0) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null && product.getStatus() == 2) { // 已售状态
                product.setStatus(1); // 恢复在售
                productMapper.updateById(product);
                log.info("恢复商品状态为在售, productId={}", product.getProductId());
            }
        }

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

        // 记录操作日志
        User buyer = userMapper.selectById(userId);
        if (buyer != null) {
            operationLogService.recordLog(
                    userId,
                    buyer.getNickname(),
                    "订单",
                    "完成订单",
                    "完成订单 " + order.getOrderNo(),
                    "PUT",
                    "/order/" + orderId + "/complete",
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    orderId,
                    "order"
            );
        }

        // 更新商品状态为已售
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus(2); // 已售
            productMapper.updateById(product);
        }

        log.info("订单完成成功, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestRefund(Long orderId, Long buyerId) {
        log.info("申请退款, orderId={}, buyerId={}", orderId, buyerId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限：只有买家可以申请退款
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有买家可以申请退款");
        }

        // 检查订单状态：只有进行中的订单可以申请退款
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确，无法申请退款");
        }

        // 更新订单状态为退款中
        order.setStatus(4);
        updateById(order);

        // 记录操作日志
        User buyer = userMapper.selectById(buyerId);
        if (buyer != null) {
            operationLogService.recordLog(
                    buyerId,
                    buyer.getNickname(),
                    "订单",
                    "申请退款",
                    "申请退款 " + order.getOrderNo(),
                    "PUT",
                    "/order/" + orderId + "/refund-request",
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    orderId,
                    "order"
            );
        }

        log.info("退款申请成功, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long orderId, Long sellerId) {
        log.info("同意退款, orderId={}, sellerId={}", orderId, sellerId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限：只有卖家可以同意退款
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家可以同意退款");
        }

        // 检查订单状态：只有退款中的订单可以同意退款
        if (order.getStatus() != 4) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确，无法同意退款");
        }

        // 更新订单状态为已取消
        order.setStatus(3);
        updateById(order);

        // 记录操作日志
        User seller = userMapper.selectById(sellerId);
        if (seller != null) {
            operationLogService.recordLog(
                    sellerId,
                    seller.getNickname(),
                    "订单",
                    "同意退款",
                    "同意退款 " + order.getOrderNo(),
                    "PUT",
                    "/order/" + orderId + "/refund-approve",
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    orderId,
                    "order"
            );
        }

        // 恢复商品状态为在售
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus(1); // 在售
            productMapper.updateById(product);
        }

        log.info("退款成功, orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(Long orderId, Long sellerId) {
        log.info("拒绝退款, orderId={}, sellerId={}", orderId, sellerId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限：只有卖家可以拒绝退款
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有卖家可以拒绝退款");
        }

        // 检查订单状态：只有退款中的订单可以拒绝退款
        if (order.getStatus() != 4) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确，无法拒绝退款");
        }

        // 恢复订单状态为进行中
        order.setStatus(1);
        updateById(order);

        // 记录操作日志
        User seller = userMapper.selectById(sellerId);
        if (seller != null) {
            operationLogService.recordLog(
                    sellerId,
                    seller.getNickname(),
                    "订单",
                    "拒绝退款",
                    "拒绝退款 " + order.getOrderNo(),
                    "PUT",
                    "/order/" + orderId + "/refund-reject",
                    null,
                    null,
                    null,
                    null,
                    1,
                    null,
                    orderId,
                    "order"
            );
        }

        log.info("拒绝退款成功, orderId={}", orderId);
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

        // 手动转换 Long 和 BigDecimal 为 String（避免 JavaScript 精度丢失）
        vo.setOrderId(order.getOrderId() != null ? String.valueOf(order.getOrderId()) : null);
        vo.setProductId(order.getProductId() != null ? String.valueOf(order.getProductId()) : null);
        vo.setBuyerId(order.getBuyerId() != null ? String.valueOf(order.getBuyerId()) : null);
        vo.setSellerId(order.getSellerId() != null ? String.valueOf(order.getSellerId()) : null);

        // 查询商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            // TODO: 从 product_image 表获取第一张图片
            // 计算单价
            vo.setUnitPrice(product.getPrice() != null ? String.valueOf(product.getPrice()) : null);
        }

        // 设置数量
        vo.setQuantity(quantity);
        vo.setTotalPrice(order.getAmount() != null ? String.valueOf(order.getAmount()) : null);

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
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            case 4 -> "已退款";
            default -> "未知状态";
        };
    }

    @Override
    public int countByUserId(Long userId) {
        log.info("统计用户订单数量, userId={}", userId);

        // 统计作为买家和卖家的所有订单（包括已取消、已退款）
        // 使用 LambdaQueryWrapper 确保正确过滤用户ID
        LambdaQueryWrapper<Order> buyerWrapper = new LambdaQueryWrapper<>();
        buyerWrapper.eq(Order::getBuyerId, userId);
        long buyerCount = count(buyerWrapper);

        LambdaQueryWrapper<Order> sellerWrapper = new LambdaQueryWrapper<>();
        sellerWrapper.eq(Order::getSellerId, userId);
        long sellerCount = count(sellerWrapper);

        log.info("用户订单统计结果, userId={}, buyerCount={}, sellerCount={}, total={}",
            userId, buyerCount, sellerCount, buyerCount + sellerCount);

        return Math.toIntExact(buyerCount + sellerCount);
    }

    @Override
    public int countBuyerOrders(Long userId) {
        log.info("统计用户买家订单数量（我买到的）, userId={}", userId);

        // 只统计作为买家的订单
        LambdaQueryWrapper<Order> buyerWrapper = new LambdaQueryWrapper<>();
        buyerWrapper.eq(Order::getBuyerId, userId);
        long buyerCount = count(buyerWrapper);

        log.info("用户买家订单统计结果, userId={}, buyerCount={}", userId, buyerCount);

        return Math.toIntExact(buyerCount);
    }
}
