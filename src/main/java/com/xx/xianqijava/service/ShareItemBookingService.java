package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.BookingApproveDTO;
import com.xx.xianqijava.dto.BookingReturnDTO;
import com.xx.xianqijava.dto.ShareItemBookingCreateDTO;
import com.xx.xianqijava.entity.ShareItemBooking;
import com.xx.xianqijava.vo.ShareItemBookingVO;

/**
 * 共享物品预约借用服务接口
 */
public interface ShareItemBookingService extends IService<ShareItemBooking> {

    /**
     * 创建预约借用
     *
     * @param createDTO 预约借用信息
     * @param borrowerId 借用者ID
     * @return 预约借用VO
     */
    ShareItemBookingVO createBooking(ShareItemBookingCreateDTO createDTO, Long borrowerId);

    /**
     * 审批预约借用
     *
     * @param approveDTO 审批信息
     * @param ownerId    物品所有者ID
     * @return 预约借用VO
     */
    ShareItemBookingVO approveBooking(BookingApproveDTO approveDTO, Long ownerId);

    /**
     * 取消预约
     *
     * @param bookingId   预约ID
     * @param borrowerId 借用者ID
     */
    void cancelBooking(Long bookingId, Long borrowerId);

    /**
     * 确认归还
     *
     * @param returnDTO  归还信息
     * @param ownerId   物品所有者ID
     * @return 预约借用VO
     */
    ShareItemBookingVO confirmReturn(BookingReturnDTO returnDTO, Long ownerId);

    /**
     * 退还押金
     *
     * @param bookingId   预约ID
     * @param ownerId    物品所有者ID
     */
    void returnDeposit(Long bookingId, Long ownerId);

    /**
     * 获取预约详情
     *
     * @param bookingId 预约ID
     * @return 预约借用VO
     */
    ShareItemBookingVO getBookingDetail(Long bookingId);

    /**
     * 获取我的预约列表（作为借用者）
     *
     * @param page        分页参数
     * @param borrowerId 借用者ID
     * @return 预约列表
     */
    IPage<ShareItemBookingVO> getMyBookings(Page<ShareItemBooking> page, Long borrowerId);

    /**
     * 获取我收到的预约列表（作为所有者）
     *
     * @param page    分页参数
     * @param ownerId 所有者ID
     * @return 预约列表
     */
    IPage<ShareItemBookingVO> getReceivedBookings(Page<ShareItemBooking> page, Long ownerId);
}
