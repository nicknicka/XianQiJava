package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.QuickReplyDTO;
import com.xx.xianqijava.entity.QuickReply;
import com.xx.xianqijava.vo.QuickReplyVO;

/**
 * 快捷回复服务接口
 */
public interface QuickReplyService extends IService<QuickReply> {

    /**
     * 创建快捷回复
     *
     * @param dto   快捷回复信息
     * @param userId 用户ID
     * @return 快捷回复VO
     */
    QuickReplyVO createQuickReply(QuickReplyDTO dto, Long userId);

    /**
     * 更新快捷回复
     *
     * @param replyId 快捷回复ID
     * @param dto     快捷回复信息
     * @param userId  用户ID
     */
    void updateQuickReply(Long replyId, QuickReplyDTO dto, Long userId);

    /**
     * 删除快捷回复
     *
     * @param replyId 快捷回复ID
     * @param userId  用户ID
     */
    void deleteQuickReply(Long replyId, Long userId);

    /**
     * 获取用户的快捷回复列表（包含系统预设）
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 快捷回复列表
     */
    IPage<QuickReplyVO> getQuickReplyList(Long userId, Page<QuickReply> page);

    /**
     * 获取系统预设的快捷回复
     *
     * @return 系统预设快捷回复列表
     */
    java.util.List<QuickReplyVO> getSystemQuickReplies();
}
