package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.RefundCreateDTO;
import com.xx.xianqijava.dto.RefundLogisticsDTO;
import com.xx.xianqijava.entity.RefundRecord;
import com.xx.xianqijava.vo.RefundVO;

/**
 * 退款记录服务接口
 */
public interface RefundRecordService extends IService<RefundRecord> {

    /**
     * 创建退款申请
     */
    RefundVO createRefund(RefundCreateDTO createDTO, Long buyerId);

    /**
     * 获取退款详情
     */
    RefundVO getRefundDetail(Long refundId, Long userId);

    /**
     * 分页查询买家的退款列表
     */
    IPage<RefundVO> getBuyerRefundList(Page<RefundRecord> page, Long buyerId, Integer status);

    /**
     * 分页查询卖家的退款列表
     */
    IPage<RefundVO> getSellerRefundList(Page<RefundRecord> page, Long sellerId, Integer status);

    /**
     * 取消退款申请
     */
    void cancelRefund(Long refundId, Long buyerId);

    /**
     * 同意退款申请（卖家）
     */
    void approveRefund(Long refundId, Long sellerId);

    /**
     * 拒绝退款申请（卖家）
     */
    void rejectRefund(Long refundId, Long sellerId, String rejectReason);

    /**
     * 填写退货物流（买家）
     */
    void fillLogistics(Long refundId, Long buyerId, RefundLogisticsDTO logisticsDTO);

    /**
     * 确认收货并完成退款（卖家）
     */
    void confirmReturn(Long refundId, Long sellerId);

    /**
     * 生成退款单号
     */
    String generateRefundNo();
}
