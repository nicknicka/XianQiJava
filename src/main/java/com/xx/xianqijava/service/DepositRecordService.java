package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.DepositPayDTO;
import com.xx.xianqijava.dto.DepositRefundDTO;
import com.xx.xianqijava.entity.DepositRecord;
import com.xx.xianqijava.vo.DepositRecordVO;

/**
 * 押金记录服务接口
 */
public interface DepositRecordService extends IService<DepositRecord> {

    /**
     * 支付押金
     *
     * @param payDTO 支付信息
     * @param userId 用户ID
     * @return 押金记录VO
     */
    DepositRecordVO payDeposit(DepositPayDTO payDTO, Long userId);

    /**
     * 退还押金
     *
     * @param refundDTO 退还信息
     * @param ownerId   物品所有者ID
     * @return 押金记录VO
     */
    DepositRecordVO refundDeposit(DepositRefundDTO refundDTO, Long ownerId);

    /**
     * 扣除押金
     *
     * @param recordId     押金记录ID
     * @param deductReason 扣除原因
     * @param ownerId      物品所有者ID
     */
    void deductDeposit(Long recordId, String deductReason, Long ownerId);

    /**
     * 获取押金记录详情
     *
     * @param recordId 记录ID
     * @return 押金记录VO
     */
    DepositRecordVO getDepositRecord(Long recordId);

    /**
     * 获取我的押金记录列表
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 押金记录列表
     */
    IPage<DepositRecordVO> getMyDepositRecords(Page<DepositRecord> page, Long userId);

    /**
     * 根据预约ID获取押金记录
     *
     * @param bookingId 预约ID
     * @return 押金记录VO
     */
    DepositRecordVO getDepositByBookingId(Long bookingId);
}
