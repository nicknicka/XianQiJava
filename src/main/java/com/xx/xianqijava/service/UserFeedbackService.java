package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.UserFeedbackDTO;
import com.xx.xianqijava.entity.UserFeedback;
import com.xx.xianqijava.vo.UserFeedbackVO;

/**
 * 用户反馈服务接口
 */
public interface UserFeedbackService extends IService<UserFeedback> {

    /**
     * 创建用户反馈
     *
     * @param dto   反馈信息
     * @param userId 用户ID（可为null）
     * @return 反馈VO
     */
    UserFeedbackVO createUserFeedback(UserFeedbackDTO dto, Long userId);

    /**
     * 获取我的反馈列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 反馈列表
     */
    IPage<UserFeedbackVO> getMyFeedback(Long userId, Page<UserFeedback> page);
}
