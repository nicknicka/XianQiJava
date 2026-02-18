package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.EvaluationCreateDTO;
import com.xx.xianqijava.entity.Evaluation;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.EvaluationMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.EvaluationService;
import com.xx.xianqijava.vo.EvaluationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评价服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationVO createEvaluation(EvaluationCreateDTO createDTO, Long evaluatorId) {
        log.info("创建评价, evaluatorId={}, orderId={}, score={}", evaluatorId, createDTO.getOrderId(), createDTO.getRating());

        // 1. 查询订单
        Order order = orderMapper.selectById(createDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2. 检查订单状态，只有已完成的订单才能评价
        if (order.getStatus() != 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能评价已完成的订单");
        }

        // 3. 确定评价人角色和被评价人
        Long evaluatedUserId;
        boolean isBuyer;

        if (order.getBuyerId().equals(evaluatorId)) {
            // 买家评价卖家
            evaluatedUserId = order.getSellerId();
            isBuyer = true;
        } else if (order.getSellerId().equals(evaluatorId)) {
            // 卖家评价买家
            evaluatedUserId = order.getBuyerId();
            isBuyer = false;
        } else {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您无权评价该订单");
        }

        // 4. 检查是否已经评价过（order_id有唯一约束）
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getOrderId, createDTO.getOrderId());
        if (count(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已被评价");
        }

        // 5. 创建评价
        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(createDTO.getOrderId());
        evaluation.setFromUserId(evaluatorId);
        evaluation.setToUserId(evaluatedUserId);
        evaluation.setScore(createDTO.getRating());
        evaluation.setContent(createDTO.getContent());
        // 将标签字符串转换为JSON数组格式存储
        if (createDTO.getTags() != null && !createDTO.getTags().isEmpty()) {
            String[] tagArray = createDTO.getTags().split(",");
            StringBuilder jsonBuilder = new StringBuilder("[");
            for (int i = 0; i < tagArray.length; i++) {
                if (i > 0) jsonBuilder.append(",");
                jsonBuilder.append("\"").append(tagArray[i].trim()).append("\"");
            }
            jsonBuilder.append("]");
            evaluation.setTags(jsonBuilder.toString());
        }

        save(evaluation);

        // 6. 更新被评价人的信用积分
        updateUserCreditScore(evaluatedUserId, createDTO.getRating());

        log.info("评价创建成功, evalId={}", evaluation.getEvalId());

        return convertToVO(evaluation, isBuyer);
    }

    @Override
    public IPage<EvaluationVO> getOrderEvaluations(Long orderId, Page<Evaluation> page) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getOrderId, orderId)
                .orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = page(page, wrapper);
        return evaluationPage.convert(eval -> {
            // Determine if the evaluator was buyer or seller
            Order order = orderMapper.selectById(eval.getOrderId());
            boolean isBuyer = order != null && order.getBuyerId().equals(eval.getFromUserId());
            return convertToVO(eval, isBuyer);
        });
    }

    @Override
    public IPage<EvaluationVO> getUserEvaluations(Long userId, Page<Evaluation> page) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getToUserId, userId)
                .orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = page(page, wrapper);
        return evaluationPage.convert(eval -> {
            // Determine if the evaluator was buyer or seller
            Order order = orderMapper.selectById(eval.getOrderId());
            boolean isBuyer = order != null && order.getBuyerId().equals(eval.getFromUserId());
            return convertToVO(eval, isBuyer);
        });
    }

    @Override
    public Integer getUserAverageRating(Long userId) {
        List<Evaluation> evaluations = lambdaQuery()
                .eq(Evaluation::getToUserId, userId)
                .list();

        if (evaluations.isEmpty()) {
            return null;
        }

        double avg = evaluations.stream()
                .mapToInt(Evaluation::getScore)
                .average()
                .orElse(0.0);

        return (int) Math.round(avg);
    }

    /**
     * 更新用户信用积分
     * 计算规则（每次评价）：
     * - 好评（5星）：+5分
     * - 中评（3-4星）：+2分
     * - 差评（1-2星）：-5分
     */
    private void updateUserCreditScore(Long userId, Integer score) {
        int creditChange;
        if (score >= 5) {
            // 好评
            creditChange = 5;
        } else if (score >= 3) {
            // 中评
            creditChange = 2;
        } else {
            // 差评
            creditChange = -5;
        }

        User user = userMapper.selectById(userId);
        if (user != null) {
            int newCreditScore = user.getCreditScore() + creditChange;
            // 限制信用积分范围 0-100
            newCreditScore = Math.max(0, Math.min(100, newCreditScore));
            user.setCreditScore(newCreditScore);
            userMapper.updateById(user);

            log.info("更新用户信用积分, userId={}, 评分={}, 积分变化={}, 旧积分={}, 新积分={}",
                    userId, score, creditChange, user.getCreditScore() - creditChange, newCreditScore);
        }
    }

    /**
     * 转换为VO
     */
    private EvaluationVO convertToVO(Evaluation evaluation, boolean isBuyer) {
        EvaluationVO vo = new EvaluationVO();
        BeanUtil.copyProperties(evaluation, vo);

        // Map entity fields to VO fields
        vo.setEvaluationId(evaluation.getEvalId());
        vo.setEvaluatorId(evaluation.getFromUserId());
        vo.setEvaluatedUserId(evaluation.getToUserId());
        vo.setRating(evaluation.getScore());

        // Set target type based on role
        vo.setTargetType(isBuyer ? 1 : 2);

        // 查询订单信息
        Order order = orderMapper.selectById(evaluation.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
            vo.setProductId(order.getProductId());

            // 查询商品信息
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                // TODO: 从 product_image 表获取第一张图片
            }
        }

        // 查询评价人信息
        User evaluator = userMapper.selectById(evaluation.getFromUserId());
        if (evaluator != null) {
            vo.setEvaluatorNickname(evaluator.getNickname());
            vo.setEvaluatorAvatar(evaluator.getAvatar());
        }

        // 查询被评价人信息
        User evaluatedUser = userMapper.selectById(evaluation.getToUserId());
        if (evaluatedUser != null) {
            vo.setEvaluatedUserNickname(evaluatedUser.getNickname());
            vo.setEvaluatedUserAvatar(evaluatedUser.getAvatar());
        }

        return vo;
    }
}
