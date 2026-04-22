package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.EvaluationAppendDTO;
import com.xx.xianqijava.dto.EvaluationCreateDTO;
import com.xx.xianqijava.entity.Evaluation;
import com.xx.xianqijava.vo.EvaluationVO;

import java.util.List;

/**
 * 评价服务接口
 */
public interface EvaluationService extends IService<Evaluation> {

    /**
     * 创建评价
     */
    EvaluationVO createEvaluation(EvaluationCreateDTO createDTO, Long evaluatorId);

    /**
     * 追加评价
     */
    EvaluationVO appendEvaluation(Long evalId, EvaluationAppendDTO appendDTO, Long evaluatorId);

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

    /**
     * 统计用户收到的评价数量
     *
     * @param userId 用户ID
     * @return 评价数量
     */
    int countByEvaluatedUserId(Long userId);

    /**
     * 获取商品的评价列表
     *
     * @param productId 商品ID
     * @param page 分页参数
     * @return 商品评价列表
     */
    IPage<EvaluationVO> getProductEvaluations(Long productId, Page<Evaluation> page);

    /**
     * 获取评价列表（管理员）
     */
    IPage<EvaluationVO> getEvaluationList(Page<Evaluation> page, Long fromUserId, Long toUserId,
                                          Long orderId, Integer score, String keyword,
                                          String startTime, String endTime);

    /**
     * 获取评价详情
     */
    EvaluationVO getEvaluationDetail(Long evalId);

    /**
     * 删除评价
     */
    void deleteEvaluation(Long evalId);

    /**
     * 批量删除评价
     */
    int batchDeleteEvaluations(List<Long> evalIds);

    /**
     * 获取平均评分
     */
    double getAverageScore();
}
