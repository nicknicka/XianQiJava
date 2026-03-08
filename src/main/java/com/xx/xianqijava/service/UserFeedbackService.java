package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.UserFeedbackDTO;
import com.xx.xianqijava.entity.UserFeedback;
import com.xx.xianqijava.vo.UserFeedbackVO;

import java.util.Map;

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

    /**
     * 获取反馈列表（管理员）
     *
     * @param page   分页参数
     * @param type   反馈类型（可选）
     * @param status 处理状态（可选）
     * @return 反馈列表
     */
    IPage<UserFeedbackVO> getFeedbackList(Page<UserFeedback> page, String type, Integer status);

    /**
     * 获取反馈详情
     *
     * @param id 反馈ID
     * @return 反馈VO
     */
    UserFeedbackVO getFeedbackDetail(Long id);

    /**
     * 处理反馈
     *
     * @param id     反馈ID
     * @param result 处理结果
     */
    void handleFeedback(Long id, String result);

    /**
     * 删除反馈
     *
     * @param id 反馈ID
     */
    void deleteFeedback(Long id);

    /**
     * 获取反馈统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getStatistics();
}
