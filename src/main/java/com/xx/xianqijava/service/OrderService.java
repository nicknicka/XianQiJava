package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.OrderCreateDTO;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    OrderVO createOrder(OrderCreateDTO createDTO, Long buyerId);

    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 分页查询订单列表
     */
    IPage<OrderVO> getOrderList(Page<Order> page, Long userId, Integer role, Integer status);

    /**
     * 确认订单
     */
    void confirmOrder(Long orderId, Long sellerId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 完成订单
     */
    void completeOrder(Long orderId, Long userId);

    /**
     * 生成订单号
     */
    String generateOrderNo();
}
