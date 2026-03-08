package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.annotation.OperationLog;
import com.xx.xianqijava.dto.admin.OrderManageQueryDTO;
import com.xx.xianqijava.dto.admin.OrderRefundProcessDTO;
import com.xx.xianqijava.service.OrderManageService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.admin.OrderManageStatistics;
import com.xx.xianqijava.vo.admin.OrderManageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器 - 管理端
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/order")
@Tag(name = "订单管理", description = "订单管理相关接口")
@SecurityRequirement(name = "bearer-auth")
public class OrderManageController {

    private final OrderManageService orderManageService;

    /**
     * 分页查询订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询订单列表", description = "支持多种条件筛选和排序")
    public Page<OrderManageVO> getOrderList(OrderManageQueryDTO queryDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}查询订单列表，查询条件：{}", adminId, queryDTO);
        return orderManageService.getOrderList(queryDTO);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取详细信息")
    public OrderManageVO getOrderDetail(@PathVariable Long orderId) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取订单详情，订单ID：{}", adminId, orderId);
        return orderManageService.getOrderDetail(orderId);
    }

    /**
     * 处理退款申请（管理员介入）
     */
    @PostMapping("/refund/process")
    @Operation(summary = "处理退款申请", description = "管理员介入处理退款申请")
    @OperationLog(
            module = "order",
            action = "refund_process",
            description = "管理员处理退款申请"
    )
    public Boolean processRefund(@Valid @RequestBody OrderRefundProcessDTO processDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}处理退款申请，订单ID：{}，处理结果：{}",
                adminId, processDTO.getOrderId(), processDTO.getResult());
        return orderManageService.processRefund(processDTO);
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计信息", description = "获取订单总数、金额、新增数等统计数据")
    public OrderManageStatistics getOrderStatistics() {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取订单统计信息", adminId);
        return orderManageService.getOrderStatistics();
    }
}
