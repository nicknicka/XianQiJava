package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.EvaluationCreateDTO;
import com.xx.xianqijava.entity.Evaluation;
import com.xx.xianqijava.vo.EvaluationVO;

/**
 * 评价服务接口
 */
public interface EvaluationService extends IService<Evaluation> {

    /**
     * 创建评价
     */
    EvaluationVO createEvaluation(EvaluationCreateDTO createDTO, Long evaluatorId);

    /**
     * 获取订单的评价列表
     */
    IPage<EvaluationVO> getOrderEvaluations(Long orderId, Page<Evaluation> page);

    /**
     * 获取用户的评价列表（作为被评价人）
     */
    IPage<EvaluationVO> getUserEvaluations(Long userId, Page<Evaluation> page);

    /**
     * 获取用户收到的评价统计
     */
    Integer getUserAverageRating(Long userId);
}
