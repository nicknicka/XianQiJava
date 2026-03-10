package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.OrderCreateDTO;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.service.OperationLogService;
import com.xx.xianqijava.service.OrderService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.OperationLogVO;
import com.xx.xianqijava.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {

    private final OrderService orderService;
    private final OperationLogService operationLogService;

    /**
     * 创建订单
     */
    @PostMapping
    @Operation(summary = "创建订单")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO createDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建订单, userId={}, productId={}", userId, createDTO.getProductId());
        OrderVO orderVO = orderService.createOrder(createDTO, userId);
        return Result.success("订单创建成功", orderVO);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        // 将字符串转换为 Long（处理前端传递的大数值 ID）
        Long orderId = Long.parseLong(id);
        OrderVO orderVO = orderService.getOrderDetail(orderId, userId);
        return Result.success(orderVO);
    }

    /**
     * 分页查询订单列表
     */
    @GetMapping
    @Operation(summary = "分页查询订单列表")
    public Result<IPage<OrderVO>> getOrderList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色：0-买家 1-卖家") @RequestParam(defaultValue = "0") Integer role,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Page<Order> pageParam = new Page<>(page, size);
        IPage<OrderVO> orderVOPage = orderService.getOrderList(pageParam, userId, role, status);
        return Result.success(orderVOPage);
    }

    /**
     * 确认订单
     */
    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认订单")
    public Result<Void> confirmOrder(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Long orderId = Long.parseLong(id);
        log.info("确认订单, orderId={}, userId={}", orderId, userId);
        orderService.confirmOrder(orderId, userId);
        return Result.success("订单确认成功");
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Long orderId = Long.parseLong(id);
        log.info("取消订单, orderId={}, userId={}", orderId, userId);
        orderService.cancelOrder(orderId, userId);
        return Result.success("订单取消成功");
    }

    /**
     * 完成订单
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "完成订单")
    public Result<Void> completeOrder(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Long orderId = Long.parseLong(id);
        log.info("完成订单, orderId={}, userId={}", orderId, userId);
        orderService.completeOrder(orderId, userId);
        return Result.success("订单完成成功");
    }

    /**
     * 申请退款（买家）
     */
    @PutMapping("/{id}/refund-request")
    @Operation(summary = "申请退款")
    public Result<Void> requestRefund(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Long orderId = Long.parseLong(id);
        log.info("申请退款, orderId={}, userId={}", orderId, userId);
        orderService.requestRefund(orderId, userId);
        return Result.success("退款申请已提交");
    }

    /**
     * 同意退款（卖家）
     */
    @PutMapping("/{id}/refund-approve")
    @Operation(summary = "同意退款")
    public Result<Void> approveRefund(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Long orderId = Long.parseLong(id);
        log.info("同意退款, orderId={}, userId={}", orderId, userId);
        orderService.approveRefund(orderId, userId);
        return Result.success("退款成功");
    }

    /**
     * 拒绝退款（卖家）
     */
    @PutMapping("/{id}/refund-reject")
    @Operation(summary = "拒绝退款")
    public Result<Void> rejectRefund(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Long orderId = Long.parseLong(id);
        log.info("拒绝退款, orderId={}, userId={}", orderId, userId);
        orderService.rejectRefund(orderId, userId);
        return Result.success("已拒绝退款申请");
    }

    /**
     * 获取订单操作日志
     */
    @GetMapping("/{id}/logs")
    @Operation(summary = "获取订单操作日志")
    public Result<List<OperationLogVO>> getOrderLogs(
            @Parameter(description = "订单ID") @PathVariable("id") String id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        Long orderId = Long.parseLong(id);
        log.info("获取订单操作日志, orderId={}, userId={}", orderId, userId);
        List<OperationLogVO> logs = operationLogService.getOrderLogs(orderId);
        return Result.success(logs);
    }
}
