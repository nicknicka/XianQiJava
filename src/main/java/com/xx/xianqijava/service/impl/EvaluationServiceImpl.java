package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.EvaluationAppendDTO;
import com.xx.xianqijava.dto.EvaluationCreateDTO;
import com.xx.xianqijava.entity.Evaluation;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.entity.ProductImage;
import com.xx.xianqijava.mapper.EvaluationMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductImageMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.EvaluationService;
import com.xx.xianqijava.util.IdConverter;
import com.xx.xianqijava.vo.EvaluationVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ProductImageMapper productImageMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationVO createEvaluation(EvaluationCreateDTO createDTO, Long evaluatorId) {
        log.info("创建评价, evaluatorId={}, orderId={}, score={}", evaluatorId, createDTO.getOrderId(), createDTO.getRating());

        try {
            // 参数校验
            if (createDTO.getOrderId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "订单ID不能为空");
            }
            if (createDTO.getRating() == null || createDTO.getRating() < 1 || createDTO.getRating() > 5) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "评分必须在1-5星之间");
            }
            String content = createDTO.getContent();
            if (content == null || content.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "评价内容不能为空");
            }
            if (content.trim().length() > 500) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "评价内容长度不能超过500个字符");
            }

            Long orderId = IdConverter.toLong(createDTO.getOrderId());

            // 1. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.warn("订单不存在, orderId={}", createDTO.getOrderId());
                throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
            }

            // 2. 检查订单状态，只有已完成的订单才能评价
            if (order.getStatus() != 2) {
                log.warn("订单状态不允许评价, orderId={}, status={}", createDTO.getOrderId(), order.getStatus());
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
                log.warn("用户无权评价该订单, evaluatorId={}, orderId={}", evaluatorId, createDTO.getOrderId());
                throw new BusinessException(ErrorCode.FORBIDDEN, "您无权评价该订单");
            }

            // 4. 检查是否已经评价过（order_id有唯一约束）
            LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Evaluation::getOrderId, orderId);
            if (count(wrapper) > 0) {
                log.warn("订单已被评价, orderId={}", createDTO.getOrderId());
                throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已被评价");
            }

            // 5. 创建评价
            Evaluation evaluation = new Evaluation();
            evaluation.setOrderId(orderId);
            evaluation.setFromUserId(evaluatorId);
            evaluation.setToUserId(evaluatedUserId);
            evaluation.setScore(createDTO.getRating());
            evaluation.setContent(content.trim());
            evaluation.setTags(toJsonArray(parseCommaSeparatedValues(createDTO.getTags())));
            evaluation.setImages(toJsonArray(sanitizeImages(createDTO.getImages())));

            save(evaluation);

            // 6. 更新被评价人的信用积分
            updateUserCreditScore(evaluatedUserId, createDTO.getRating());

            log.info("评价创建成功, evalId={}", evaluation.getEvalId());

            return convertToVO(evaluation, isBuyer);
        } catch (BusinessException e) {
            // 业务异常直接抛出
            log.error("创建评价业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 其他异常记录日志后抛出
            log.error("创建评价失败, evaluatorId={}, orderId={}, error={}",
                    evaluatorId, createDTO.getOrderId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建评价失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluationVO appendEvaluation(Long evalId, EvaluationAppendDTO appendDTO, Long evaluatorId) {
        log.info("追加评价, evalId={}, evaluatorId={}", evalId, evaluatorId);

        if (evalId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价ID不能为空");
        }
        Evaluation evaluation = getById(evalId);
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_FOUND, "评价不存在");
        }
        if (!evaluation.getFromUserId().equals(evaluatorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您无权追加该评价");
        }
        if (evaluation.getAppendTime() != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该评价已追评");
        }

        String content = appendDTO.getContent() == null ? "" : appendDTO.getContent().trim();
        List<String> images = sanitizeImages(appendDTO.getImages());
        if (content.isEmpty() && images.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "追评内容或图片不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "追评内容长度不能超过500个字符");
        }

        evaluation.setAppendContent(content.isEmpty() ? null : content);
        evaluation.setAppendImages(toJsonArray(images));
        evaluation.setAppendTime(LocalDateTime.now());
        updateById(evaluation);

        Order order = orderMapper.selectById(evaluation.getOrderId());
        boolean isBuyer = order != null && order.getBuyerId().equals(evaluatorId);
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
        // 查询用户给出的评价（作为评价人）- 用于"我的评价"页面
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getFromUserId, userId)
                .orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = page(page, wrapper);
        return evaluationPage.convert(eval -> {
            // 当前用户就是评价人，判断其角色
            Order order = orderMapper.selectById(eval.getOrderId());
            boolean isBuyer = order != null && order.getBuyerId().equals(userId);
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
        vo.setId(String.valueOf(evaluation.getEvalId()));
        vo.setEvaluationId(String.valueOf(evaluation.getEvalId()));
        vo.setOrderId(String.valueOf(evaluation.getOrderId()));
        vo.setEvaluatorId(String.valueOf(evaluation.getFromUserId()));
        vo.setFromUserId(vo.getEvaluatorId());
        vo.setEvaluatedUserId(String.valueOf(evaluation.getToUserId()));
        vo.setToUserId(vo.getEvaluatedUserId());
        vo.setRating(evaluation.getScore());
        vo.setContent(evaluation.getContent());
        vo.setTags(parseJsonArray(evaluation.getTags()));
        vo.setImages(parseJsonArray(evaluation.getImages()));
        vo.setAppendContent(evaluation.getAppendContent());
        vo.setAppendImages(parseJsonArray(evaluation.getAppendImages()));
        vo.setAppendTime(evaluation.getAppendTime());
        vo.setHasAppend(evaluation.getAppendTime() != null);
        vo.setCreateTime(evaluation.getCreateTime());
        vo.setCreatedAt(evaluation.getCreateTime());

        // Set target type based on role
        vo.setTargetType(isBuyer ? 1 : 2);

        // 查询订单信息
        Order order = orderMapper.selectById(evaluation.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
            vo.setProductId(String.valueOf(order.getProductId()));
            // 设置商品价格（使用订单成交金额）
            vo.setProductPrice(order.getAmount());

            // 查询商品信息
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                vo.setProductName(product.getTitle());
                // 获取商品封面图
                vo.setProductImage(getProductCoverImage(product.getProductId()));
            }
        }

        // 查询评价人信息
        User evaluator = userMapper.selectById(evaluation.getFromUserId());
        if (evaluator != null) {
            vo.setEvaluatorNickname(evaluator.getNickname());
            vo.setEvaluatorName(evaluator.getNickname());
            vo.setFromUserName(evaluator.getNickname());
            vo.setEvaluatorAvatar(evaluator.getAvatar());
            vo.setFromUserAvatar(evaluator.getAvatar());
        }

        // 查询被评价人信息
        User evaluatedUser = userMapper.selectById(evaluation.getToUserId());
        if (evaluatedUser != null) {
            vo.setEvaluatedUserNickname(evaluatedUser.getNickname());
            vo.setToUserName(evaluatedUser.getNickname());
            vo.setEvaluatedUserAvatar(evaluatedUser.getAvatar());
            vo.setToUserAvatar(evaluatedUser.getAvatar());
        }

        return vo;
    }

    private List<String> parseCommaSeparatedValues(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> sanitizeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> sanitizedImages = images.stream()
                .map(image -> image == null ? "" : image.trim())
                .filter(image -> !image.isEmpty())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (sanitizedImages.size() > 3) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价图片最多上传3张");
        }
        return sanitizedImages;
    }

    private String toJsonArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "评价数据序列化失败");
        }
    }

    private List<String> parseJsonArray(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(rawValue, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return parseCommaSeparatedValues(rawValue);
        }
    }

    @Override
    public int countByEvaluatedUserId(Long userId) {
        return Math.toIntExact(lambdaQuery()
                .eq(Evaluation::getToUserId, userId)
                .count());
    }

    @Override
    public IPage<EvaluationVO> getProductEvaluations(Long productId, Page<Evaluation> page) {
        log.info("获取商品的评价列表, productId={}, page={}", productId, page.getCurrent());

        // 通过订单关联查询商品的评价
        // 1. 查询该商品相关的所有订单
        java.util.List<Long> orderIds = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getProductId, productId)
                        .eq(Order::getStatus, 2) // 已完成的订单
        ).stream().map(Order::getOrderId).collect(java.util.stream.Collectors.toList());

        if (orderIds.isEmpty()) {
            log.info("该商品暂无评价, productId={}", productId);
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        // 2. 查询这些订单的评价
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Evaluation::getOrderId, orderIds)
                .orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = page(page, wrapper);
        return evaluationPage.convert(evaluation -> convertToVO(evaluation, false));
    }

    @Override
    public IPage<EvaluationVO> getEvaluationList(Page<Evaluation> page, Long fromUserId, Long toUserId,
                                                   Long orderId, Integer score, String keyword,
                                                   String startTime, String endTime) {
        log.info("查询评价列表（管理员）, fromUserId={}, toUserId={}, score={}", fromUserId, toUserId, score);

        LambdaQueryWrapper<Evaluation> queryWrapper = new LambdaQueryWrapper<>();

        // 按评价人筛选
        if (fromUserId != null) {
            queryWrapper.eq(Evaluation::getFromUserId, fromUserId);
        }

        // 按被评价人筛选
        if (toUserId != null) {
            queryWrapper.eq(Evaluation::getToUserId, toUserId);
        }

        // 按订单筛选
        if (orderId != null) {
            queryWrapper.eq(Evaluation::getOrderId, orderId);
        }

        // 按评分筛选
        if (score != null) {
            queryWrapper.eq(Evaluation::getScore, score);
        }

        // 按关键词筛选（评价内容）
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Evaluation::getContent, keyword);
        }

        // 按时间范围筛选
        if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
            queryWrapper.between(Evaluation::getCreateTime, startTime, endTime);
        }

        queryWrapper.orderByDesc(Evaluation::getCreateTime);

        IPage<Evaluation> evaluationPage = page(page, queryWrapper);
        return evaluationPage.convert(evaluation -> convertToVO(evaluation, false));
    }

    @Override
    public EvaluationVO getEvaluationDetail(Long evalId) {
        log.info("查询评价详情, evalId={}", evalId);

        Evaluation evaluation = getById(evalId);
        if (evaluation == null) {
            throw new RuntimeException("评价不存在");
        }

        return convertToVO(evaluation, false);
    }

    @Override
    public void deleteEvaluation(Long evalId) {
        log.info("删除评价, evalId={}", evalId);

        Evaluation evaluation = getById(evalId);
        if (evaluation == null) {
            throw new RuntimeException("评价不存在");
        }

        // 删除评价
        removeById(evalId);

        log.info("评价删除成功, evalId={}", evalId);
    }

    @Override
    public int batchDeleteEvaluations(java.util.List<Long> evalIds) {
        log.info("批量删除评价, count={}", evalIds.size());

        int count = 0;
        for (Long evalId : evalIds) {
            try {
                deleteEvaluation(evalId);
                count++;
            } catch (Exception e) {
                log.error("删除评价失败, evalId={}, error={}", evalId, e.getMessage());
            }
        }

        return count;
    }

    @Override
    public double getAverageScore() {
        List<Evaluation> evaluations = lambdaQuery().list();

        if (evaluations.isEmpty()) {
            return 0.0;
        }

        return evaluations.stream()
                .mapToInt(Evaluation::getScore)
                .average()
                .orElse(0.0);
    }

    /**
     * 获取商品封面图
     */
    private String getProductCoverImage(Long productId) {
        // 从 product_image 表查询封面图
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId)
               .eq(ProductImage::getIsCover, 1)
               .eq(ProductImage::getStatus, 0)  // 0=正常，1=删除
               .last("LIMIT 1");

        ProductImage coverImage = productImageMapper.selectOne(wrapper);
        return coverImage != null ? coverImage.getImageUrl() : "";
    }
}
