package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.TransferCreateDTO;
import com.xx.xianqijava.dto.TransferRespondDTO;
import com.xx.xianqijava.entity.TransferRecord;
import com.xx.xianqijava.vo.TransferRecordVO;

import java.util.List;

/**
 * 转赠记录服务接口
 */
public interface TransferRecordService extends IService<TransferRecord> {

    /**
     * 发起转赠
     *
     * @param createDTO 转赠信息
     * @param fromUserId 转出人ID
     * @return 转赠记录VO
     */
    TransferRecordVO createTransfer(TransferCreateDTO createDTO, Long fromUserId);

    /**
     * 响应转赠（接受或拒绝）
     *
     * @param respondDTO 响应信息
     * @param toUserId   接收人ID
     * @return 转赠记录VO
     */
    TransferRecordVO respondTransfer(TransferRespondDTO respondDTO, Long toUserId);

    /**
     * 取消转赠
     *
     * @param transferId 转赠记录ID
     * @param fromUserId 转出人ID
     */
    void cancelTransfer(Long transferId, Long fromUserId);

    /**
     * 获取转赠记录详情
     *
     * @param transferId 记录ID
     * @return 转赠记录VO
     */
    TransferRecordVO getTransferRecord(Long transferId);

    /**
     * 获取我发起的转赠列表
     *
     * @param page      分页参数
     * @param fromUserId 转出人ID
     * @return 转赠记录列表
     */
    IPage<TransferRecordVO> getMySentTransfers(Page<TransferRecord> page, Long fromUserId);

    /**
     * 获取我收到的转赠列表
     *
     * @param page    分页参数
     * @param toUserId 接收人ID
     * @return 转赠记录列表
     */
    IPage<TransferRecordVO> getMyReceivedTransfers(Page<TransferRecord> page, Long toUserId);

    /**
     * 获取待确认的转赠列表
     *
     * @param toUserId 接收人ID
     * @return 转赠记录列表
     */
    List<TransferRecordVO> getPendingTransfers(Long toUserId);
}
