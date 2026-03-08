package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.dto.UserFeedbackDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserFeedback;
import com.xx.xianqijava.mapper.UserFeedbackMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.UserFeedbackService;
import com.xx.xianqijava.vo.UserFeedbackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户反馈服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackServiceImpl extends ServiceImpl<UserFeedbackMapper, UserFeedback> implements UserFeedbackService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFeedbackVO createUserFeedback(UserFeedbackDTO dto, Long userId) {
        log.info("创建用户反馈, userId={}, type={}", userId, dto.getType());

        // 创建反馈
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setContact(dto.getContact());
        feedback.setType(convertTypeToInt(dto.getType()));
        // title字段允许为空，如果前端传入了值且不为空则使用，否则设置为null
        feedback.setTitle(dto.getTitle() != null && !dto.getTitle().trim().isEmpty() ? dto.getTitle() : null);
        feedback.setContent(dto.getContent());
        // 将图片URL列表转换为JSON数组字符串（数据库images字段为json类型）
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            feedback.setImages(JSONUtil.toJsonStr(dto.getImages()));
        }
        feedback.setStatus(0); // 待处理

        save(feedback);
        log.info("用户反馈创建成功, feedbackId={}", feedback.getFeedbackId());

        return convertToVO(feedback);
    }

    /**
     * 将前端反馈类型字符串转换为数据库整型
     * @param type 前端类型：bug-功能异常，suggestion-功能建议，other-其他问题
     * @return 数据库类型：1-功能建议, 2-Bug反馈, 3-投诉, 4-其他
     */
    private Integer convertTypeToInt(String type) {
        if (type == null) {
            return 4; // 默认为"其他"
        }
        return switch (type.toLowerCase()) {
            case "bug" -> 2;
            case "suggestion" -> 1;
            case "complaint" -> 3;
            case "other" -> 4;
            default -> 4;
        };
    }

    @Override
    public IPage<UserFeedbackVO> getMyFeedback(Long userId, Page<UserFeedback> page) {
        log.info("查询我的反馈列表, userId={}", userId);

        LambdaQueryWrapper<UserFeedback> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFeedback::getUserId, userId)
                .orderByDesc(UserFeedback::getCreateTime);

        IPage<UserFeedback> feedbackPage = page(page, queryWrapper);
        return feedbackPage.convert(this::convertToVO);
    }

    @Override
    public IPage<UserFeedbackVO> getFeedbackList(Page<UserFeedback> page, String type, Integer status) {
        log.info("查询反馈列表, type={}, status={}", type, status);

        LambdaQueryWrapper<UserFeedback> queryWrapper = new LambdaQueryWrapper<>();

        // 按类型筛选
        if (type != null && !type.isEmpty()) {
            Integer typeInt = convertTypeToInt(type);
            queryWrapper.eq(UserFeedback::getType, typeInt);
        }

        // 按状态筛选
        if (status != null) {
            queryWrapper.eq(UserFeedback::getStatus, status);
        }

        queryWrapper.orderByDesc(UserFeedback::getCreateTime);

        IPage<UserFeedback> feedbackPage = page(page, queryWrapper);
        return feedbackPage.convert(this::convertToVO);
    }

    @Override
    public UserFeedbackVO getFeedbackDetail(Long id) {
        log.info("查询反馈详情, id={}", id);

        UserFeedback feedback = getById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }

        return convertToVO(feedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFeedback(Long id, String result) {
        log.info("处理反馈, id={}, result={}", id, result);

        UserFeedback feedback = getById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }

        feedback.setStatus(2); // 已处理
        feedback.setHandleNote(result);
        feedback.setHandleTime(java.time.LocalDateTime.now());
        updateById(feedback);

        log.info("反馈处理成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedback(Long id) {
        log.info("删除反馈, id={}", id);

        UserFeedback feedback = getById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }

        removeById(id);
        log.info("反馈删除成功, id={}", id);
    }

    @Override
    public Map<String, Object> getStatistics() {
        log.info("查询反馈统计数据");

        Map<String, Object> statistics = new HashMap<>();

        // 总数
        long totalCount = count();
        statistics.put("totalCount", totalCount);

        // 按状态统计
        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("pending", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getStatus, 0)));
        statusStats.put("processing", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getStatus, 1)));
        statusStats.put("resolved", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getStatus, 2)));
        statusStats.put("rejected", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getStatus, 3)));
        statistics.put("statusStats", statusStats);

        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        typeStats.put("suggestion", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getType, 1)));
        typeStats.put("bug", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getType, 2)));
        typeStats.put("complaint", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getType, 3)));
        typeStats.put("other", count(new LambdaQueryWrapper<UserFeedback>().eq(UserFeedback::getType, 4)));
        statistics.put("typeStats", typeStats);

        return statistics;
    }

    /**
     * 转换为VO
     */
    private UserFeedbackVO convertToVO(UserFeedback feedback) {
        UserFeedbackVO vo = new UserFeedbackVO();
        BeanUtil.copyProperties(feedback, vo);

        // 设置类型描述
        vo.setTypeDesc(getTypeDesc(feedback.getType()));

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(feedback.getStatus()));

        // 获取用户信息
        if (feedback.getUserId() != null) {
            User user = userMapper.selectById(feedback.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }
        }

        return vo;
    }

    /**
     * 获取反馈类型描述
     */
    private String getTypeDesc(Integer type) {
        switch (type) {
            case 1:
                return "功能建议";
            case 2:
                return "Bug反馈";
            case 3:
                return "投诉";
            case 4:
                return "其他";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取处理状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "处理中";
            case 2:
                return "已处理";
            case 3:
                return "已驳回";
            default:
                return "未知状态";
        }
    }
}
