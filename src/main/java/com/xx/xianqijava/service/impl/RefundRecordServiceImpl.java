package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.RefundCreateDTO;
import com.xx.xianqijava.dto.RefundLogisticsDTO;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.entity.ProductImage;
import com.xx.xianqijava.entity.RefundRecord;
import com.xx.xianqijava.enums.RefundStatus;
import com.xx.xianqijava.enums.RefundType;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductImageMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.RefundRecordMapper;
import com.xx.xianqijava.service.RefundRecordService;
import com.xx.xianqijava.util.IdConverter;
import com.xx.xianqijava.vo.RefundVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 退款记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundRecordServiceImpl extends ServiceImpl<RefundRecordMapper, RefundRecord> implements RefundRecordService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ObjectMapper objectMapper;

    private static final AtomicInteger REFUND_COUNTER = new AtomicInteger(0);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundVO createRefund(RefundCreateDTO createDTO, Long buyerId) {
        log.info("创建退款申请, buyerId={}, orderId={}", buyerId, createDTO.getOrderId());

        // 1. 查询订单
        Order order = orderMapper.selectById(createDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2. 检查订单是否属于当前用户
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }

        // 3. 检查订单状态（只有进行中或已完成的订单可以申请退款）
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "当前订单状态不允许申请退款");
        }

        // 4. 检查是否已有退款记录
        LambdaQueryWrapper<RefundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundRecord::getOrderId, createDTO.getOrderId())
                .in(RefundRecord::getStatus, 0, 1, 3); // 待审核、已同意、退货中
        RefundRecord existingRefund = getOne(wrapper);
        if (existingRefund != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已有退款申请在进行中");
        }

        // 5. 检查退款金额不能超过订单金额
        if (createDTO.getRefundAmount().compareTo(order.getAmount()) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "退款金额不能超过订单金额");
        }

        // 6. 创建退款记录
        RefundRecord refund = new RefundRecord();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(IdConverter.toLong(createDTO.getOrderId()));
        refund.setRefundAmount(createDTO.getRefundAmount());
        refund.setRefundReason(createDTO.getRefundReason());
        refund.setRefundType(createDTO.getRefundType());
        refund.setStatus(RefundStatus.PENDING.getCode());

        // 保存凭证图片
        if (createDTO.getEvidenceImages() != null && !createDTO.getEvidenceImages().isEmpty()) {
            try {
                refund.setEvidenceImages(objectMapper.writeValueAsString(createDTO.getEvidenceImages()));
            } catch (JsonProcessingException e) {
                log.error("JSON序列化失败", e);
            }
        }

        refund.setRemark(createDTO.getRemark());

        save(refund);

        // 7. 更新订单状态为退款中
        order.setStatus(4);
        orderMapper.updateById(order);

        log.info("退款申请创建成功, refundId={}, refundNo={}", refund.getRefundId(), refund.getRefundNo());

        return convertToVO(refund, order);
    }

    @Override
    public RefundVO getRefundDetail(Long refundId, Long userId) {
        RefundRecord refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限（只有买家和卖家可以查看）
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看此退款记录");
        }

        return convertToVO(refund, order);
    }

    @Override
    public IPage<RefundVO> getBuyerRefundList(Page<RefundRecord> page, Long buyerId, Integer status) {
        // 查询用户作为买家的订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getBuyerId, buyerId);
        List<Order> orders = orderMapper.selectList(orderWrapper);

        if (orders.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        // 获取订单ID列表
        List<Long> orderIds = orders.stream().map(Order::getOrderId).collect(Collectors.toList());

        // 查询退款记录
        LambdaQueryWrapper<RefundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RefundRecord::getOrderId, orderIds);
        if (status != null) {
            wrapper.eq(RefundRecord::getStatus, status);
        }
        wrapper.orderByDesc(RefundRecord::getCreateTime);

        Page<RefundRecord> resultPage = page(page, wrapper);

        // 转换为VO
        Page<RefundVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<RefundVO> voList = resultPage.getRecords().stream()
                .map(refund -> {
                    Order order = orderMapper.selectById(refund.getOrderId());
                    return convertToVO(refund, order);
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<RefundVO> getSellerRefundList(Page<RefundRecord> page, Long sellerId, Integer status) {
        // 查询用户作为卖家的订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getSellerId, sellerId);
        List<Order> orders = orderMapper.selectList(orderWrapper);

        if (orders.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        // 获取订单ID列表
        List<Long> orderIds = orders.stream().map(Order::getOrderId).collect(Collectors.toList());

        // 查询退款记录
        LambdaQueryWrapper<RefundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RefundRecord::getOrderId, orderIds);
        if (status != null) {
            wrapper.eq(RefundRecord::getStatus, status);
        }
        wrapper.orderByDesc(RefundRecord::getCreateTime);

        Page<RefundRecord> resultPage = page(page, wrapper);

        // 转换为VO
        Page<RefundVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<RefundVO> voList = resultPage.getRecords().stream()
                .map(refund -> {
                    Order order = orderMapper.selectById(refund.getOrderId());
                    return convertToVO(refund, order);
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRefund(Long refundId, Long buyerId) {
        log.info("取消退款申请, refundId={}, buyerId={}", refundId, buyerId);

        RefundRecord refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此退款记录");
        }

        // 检查状态（只有待审核状态可以取消）
        if (refund.getStatus() != RefundStatus.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不允许取消退款");
        }

        // 更新退款状态
        refund.setStatus(RefundStatus.CANCELLED.getCode());
        updateById(refund);

        // 恢复订单状态
        order.setStatus(1); // 进行中
        orderMapper.updateById(order);

        log.info("退款申请已取消, refundId={}", refundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long refundId, Long sellerId) {
        log.info("同意退款申请, refundId={}, sellerId={}", refundId, sellerId);

        RefundRecord refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此退款记录");
        }

        // 检查状态
        if (refund.getStatus() != RefundStatus.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不允许操作");
        }

        // 更新退款状态
        if (refund.getRefundType() == RefundType.REFUND_ONLY.getCode()) {
            // 仅退款，直接完成
            refund.setStatus(RefundStatus.COMPLETED.getCode());
            refund.setFinishTime(LocalDateTime.now());

            // 更新订单状态
            order.setStatus(2); // 已完成
            orderMapper.updateById(order);
        } else {
            // 退货退款，等待买家填写物流
            refund.setStatus(RefundStatus.APPROVED.getCode());
        }

        updateById(refund);

        log.info("退款申请已同意, refundId={}", refundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(Long refundId, Long sellerId, String rejectReason) {
        log.info("拒绝退款申请, refundId={}, sellerId={}, reason={}", refundId, sellerId, rejectReason);

        RefundRecord refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此退款记录");
        }

        // 检查状态
        if (refund.getStatus() != RefundStatus.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不允许操作");
        }

        // 更新退款状态
        refund.setStatus(RefundStatus.REJECTED.getCode());
        refund.setRejectReason(rejectReason);
        updateById(refund);

        // 恢复订单状态
        order.setStatus(1); // 进行中
        orderMapper.updateById(order);

        log.info("退款申请已拒绝, refundId={}", refundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fillLogistics(Long refundId, Long buyerId, RefundLogisticsDTO logisticsDTO) {
        log.info("填写退货物流, refundId={}, buyerId={}, company={}, no={}",
                refundId, buyerId, logisticsDTO.getLogisticsCompany(), logisticsDTO.getLogisticsNo());

        RefundRecord refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此退款记录");
        }

        // 检查状态（只有已同意状态可以填写物流）
        if (refund.getStatus() != RefundStatus.APPROVED.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不允许填写物流");
        }

        // 检查退款类型
        if (refund.getRefundType() != RefundType.RETURN_AND_REFUND.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅退款类型无需填写物流");
        }

        // 更新物流信息
        refund.setLogisticsCompany(logisticsDTO.getLogisticsCompany());
        refund.setLogisticsNo(logisticsDTO.getLogisticsNo());
        refund.setLogisticsTime(LocalDateTime.now());
        refund.setStatus(RefundStatus.RETURNING.getCode());
        updateById(refund);

        log.info("退货物流已填写, refundId={}", refundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(Long refundId, Long sellerId) {
        log.info("确认收货并完成退款, refundId={}, sellerId={}", refundId, sellerId);

        RefundRecord refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }

        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 检查权限
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此退款记录");
        }

        // 检查状态（只有退货中状态可以确认收货）
        if (refund.getStatus() != RefundStatus.RETURNING.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不允许确认收货");
        }

        // 更新退款状态为已完成
        refund.setStatus(RefundStatus.COMPLETED.getCode());
        refund.setFinishTime(LocalDateTime.now());
        updateById(refund);

        // 更新订单状态
        order.setStatus(2); // 已完成
        orderMapper.updateById(order);

        log.info("已确认收货并完成退款, refundId={}", refundId);
    }

    @Override
    public String generateRefundNo() {
        // 格式: RF + yyyyMMdd + 4位序号
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = REFUND_COUNTER.getAndUpdate(i -> i >= 9999 ? 1 : i + 1);
        return String.format("RF%s%04d", dateStr, seq);
    }

    /**
     * 转换为VO
     */
    private RefundVO convertToVO(RefundRecord refund, Order order) {
        RefundVO vo = new RefundVO();
        BeanUtil.copyProperties(refund, vo);

        // 设置订单信息
        vo.setOrderId(String.valueOf(order.getOrderId()));
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(String.valueOf(order.getBuyerId()));
        vo.setSellerId(String.valueOf(order.getSellerId()));
        vo.setProductId(String.valueOf(order.getProductId()));

        // 查询商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductName(product.getTitle());

            // 查询商品封面图
            LambdaQueryWrapper<ProductImage> imgWrapper = new LambdaQueryWrapper<>();
            imgWrapper.eq(ProductImage::getProductId, product.getProductId())
                    .eq(ProductImage::getIsCover, 1);
            ProductImage coverImage = productImageMapper.selectOne(imgWrapper);
            if (coverImage != null) {
                vo.setProductImage(coverImage.getImageThumbnailUrl());
            }
        }

        // 解析状态和类型描述
        RefundStatus statusEnum = RefundStatus.getByCode(refund.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDesc() : null);
        RefundType typeEnum = RefundType.getByCode(refund.getRefundType());
        vo.setRefundTypeDesc(typeEnum != null ? typeEnum.getDesc() : null);

        // 解析凭证图片
        if (refund.getEvidenceImages() != null) {
            try {
                List<String> images = objectMapper.readValue(refund.getEvidenceImages(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                vo.setEvidenceImages(images);
            } catch (Exception e) {
                log.error("JSON解析失败", e);
            }
        }

        return vo;
    }
}
