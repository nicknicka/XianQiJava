package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.OrderManageQueryDTO;
import com.xx.xianqijava.dto.admin.OrderRefundProcessDTO;
import com.xx.xianqijava.vo.admin.OrderManageVO;
import com.xx.xianqijava.vo.admin.OrderManageStatistics;

/**
 * 订单管理服务接口 - 管理端
 */
public interface OrderManageService {

    /**
     * 分页查询订单列表
     *
     * @param queryDTO 查询条件
     * @return 订单分页数据
     */
    Page<OrderManageVO> getOrderList(OrderManageQueryDTO queryDTO);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderManageVO getOrderDetail(Long orderId);

    /**
     * 处理退款申请（管理员介入）
     *
     * @param processDTO 处理DTO
     * @return 是否成功
     */
    Boolean processRefund(OrderRefundProcessDTO processDTO);

    /**
     * 获取订单统计信息
     *
     * @return 统计信息
     */
    OrderManageStatistics getOrderStatistics();
}
