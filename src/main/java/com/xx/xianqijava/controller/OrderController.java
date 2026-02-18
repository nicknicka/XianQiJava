package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.OrderCreateDTO;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.service.OrderService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        OrderVO orderVO = orderService.getOrderDetail(id, userId);
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
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("确认订单, orderId={}, userId={}", id, userId);
        orderService.confirmOrder(id, userId);
        return Result.success("订单确认成功");
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("取消订单, orderId={}, userId={}", id, userId);
        orderService.cancelOrder(id, userId);
        return Result.success("订单取消成功");
    }

    /**
     * 完成订单
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "完成订单")
    public Result<Void> completeOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("完成订单, orderId={}, userId={}", id, userId);
        orderService.completeOrder(id, userId);
        return Result.success("订单完成成功");
    }
}
