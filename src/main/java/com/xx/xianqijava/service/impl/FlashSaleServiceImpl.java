package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.FlashSaleProduct;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.entity.FlashSaleOrderExt;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.enums.FlashSaleStatus;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.FlashSaleOrderExtMapper;
import com.xx.xianqijava.mapper.FlashSaleProductMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.service.FlashSaleService;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.FlashSaleSessionVO;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 秒杀服务实现类（已简化，只使用场次和商品表）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl extends ServiceImpl<FlashSaleSessionMapper, FlashSaleSession> implements FlashSaleService {

    private final FlashSaleProductMapper flashProductMapper;
    private final ProductMapper productMapper;
    private final FlashSaleSessionMapper flashSaleSessionMapper;
    private final FlashSaleOrderExtMapper flashSaleOrderExtMapper;
    private final OrderMapper orderMapper;

    @Override
    public FlashSaleSession getCurrentSession() {
        LocalDateTime now = LocalDateTime.now();

        // 查询当前进行中的场次
        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(FlashSaleSession::getStartTime, now)
               .ge(FlashSaleSession::getEndTime, now)
               .orderByAsc(FlashSaleSession::getStartTime)
               .last("LIMIT 1");

        return flashSaleSessionMapper.selectOne(wrapper);
    }

    @Override
    public List<ProductVO> getCurrentFlashSaleProducts(Integer limit) {
        // 获取当前场次
        FlashSaleSession currentSession = getCurrentSession();
        if (currentSession == null) {
            return List.of();
        }

        // 查询场次商品
        LambdaQueryWrapper<FlashSaleProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleProduct::getSessionId, currentSession.getSessionId())
               .orderByDesc(FlashSaleProduct::getSortOrder)
               .last("LIMIT " + (limit != null ? limit : 10));

        List<FlashSaleProduct> flashProducts = flashProductMapper.selectList(wrapper);

        // 转换为 ProductVO，包含秒杀价
        return flashProducts.stream()
                .map(this::convertToProductVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleProductVO> getSessionProducts(Long sessionId) {
        // 查询秒杀商品
        LambdaQueryWrapper<FlashSaleProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleProduct::getSessionId, sessionId)
               .orderByDesc(FlashSaleProduct::getSortOrder);

        List<FlashSaleProduct> flashProducts = flashProductMapper.selectList(wrapper);

        return flashProducts.stream()
                .map(fp -> convertToFlashSaleProductVO(fp, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 查询所有需要更新状态的场次
        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FlashSaleSession::getStatus,
                   FlashSaleStatus.NOT_STARTED.getCode(),
                   FlashSaleStatus.IN_PROGRESS.getCode());

        List<FlashSaleSession> sessions = flashSaleSessionMapper.selectList(wrapper);

        for (FlashSaleSession session : sessions) {
            Integer newStatus;

            if (now.isBefore(session.getStartTime())) {
                newStatus = FlashSaleStatus.NOT_STARTED.getCode();
            } else if (now.isAfter(session.getEndTime())) {
                newStatus = FlashSaleStatus.ENDED.getCode();
            } else {
                newStatus = FlashSaleStatus.IN_PROGRESS.getCode();
            }

            if (!session.getStatus().equals(newStatus)) {
                session.setStatus(newStatus);
                flashSaleSessionMapper.updateById(session);
                log.info("场次状态已更新, sessionId={}, status={}", session.getSessionId(), newStatus);
            }
        }
    }

    /**
     * 转换为 ProductVO（包含秒杀价）
     */
    private ProductVO convertToProductVO(FlashSaleProduct flashProduct) {
        Product product = productMapper.selectById(flashProduct.getProductId());
        if (product == null) {
            log.warn("商品不存在, productId={}", flashProduct.getProductId());
            return null;
        }

        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setFlashPrice(flashProduct.getFlashPrice());
        vo.setIsFlashSale(true);

        FlashSaleSession session = flashSaleSessionMapper.selectById(flashProduct.getSessionId());
        if (session != null) {
            vo.setFlashEndTime(session.getEndTime().toString());
        }

        return vo;
    }

    /**
     * 转换为 FlashSaleProductVO
     */
    private FlashSaleProductVO convertToFlashSaleProductVO(FlashSaleProduct flashProduct, FlashSaleSession session) {
        FlashSaleProductVO vo = new FlashSaleProductVO();
        vo.setId(flashProduct.getProductId());
        vo.setSessionId(flashProduct.getSessionId());
        vo.setSeckillPrice(flashProduct.getFlashPrice());
        vo.setStock(flashProduct.getStockCount());
        vo.setSoldCount(flashProduct.getSoldCount());
        vo.setLimitPerUser(flashProduct.getLimitPerUser() != null ? flashProduct.getLimitPerUser() : 1);

        if (flashProduct.getStockCount() > 0) {
            vo.setSoldPercent(flashProduct.getSoldCount() * 100 / flashProduct.getStockCount());
        }

        Product product = productMapper.selectById(flashProduct.getProductId());
        if (product != null) {
            vo.setTitle(product.getTitle());
            vo.setDescription(product.getDescription());
            vo.setCoverImage(getProductCoverImage(product.getProductId()));
            vo.setOriginalPrice(product.getPrice());
            vo.setPrice(product.getPrice());

            if (flashProduct.getFlashPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discount = flashProduct.getFlashPrice()
                        .divide(product.getPrice(), 1, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.TEN);
                vo.setDiscount(discount.intValue());
            }

            vo.setLocation(product.getLocation());
            vo.setCondition(product.getConditionLevel() != null ? product.getConditionLevel().toString() : "");
            vo.setCategoryId(product.getCategoryId() != null ? product.getCategoryId().intValue() : null);
        }

        if (session != null) {
            vo.setEndTime(session.getEndTime().toString());

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(session.getStartTime())) {
                vo.setStatus("upcoming");
            } else if (now.isAfter(session.getEndTime())) {
                vo.setStatus("ended");
            } else {
                vo.setStatus("ongoing");
            }
        }

        return vo;
    }

    private String getProductCoverImage(Long productId) {
        // TODO: 从 product_image 表查询封面图
        return "";
    }

    // ========== 场次相关方法 ==========

    @Override
    public List<FlashSaleSessionVO> getActiveSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startRange = now.minusHours(2);
        LocalDateTime endRange = now.plusHours(24);

        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FlashSaleSession::getEndTime, startRange)
               .le(FlashSaleSession::getStartTime, endRange)
               .orderByAsc(FlashSaleSession::getStartTime);

        List<FlashSaleSession> sessions = flashSaleSessionMapper.selectList(wrapper);

        return sessions.stream()
                .map(session -> {
                    FlashSaleSessionVO vo = new FlashSaleSessionVO();
                    vo.setSessionId(session.getSessionId());
                    vo.setName(session.getName());
                    vo.setDescription(session.getDescription());

                    if (session.getSessionTime() != null) {
                        vo.setTime(session.getSessionTime());
                    } else if (session.getStartTime() != null) {
                        vo.setTime(session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    }

                    vo.setStartTime(session.getStartTime() != null ? session.getStartTime().toString() : "");
                    vo.setEndTime(session.getEndTime() != null ? session.getEndTime().toString() : "");

                    if (now.isBefore(session.getStartTime())) {
                        vo.setStatus("upcoming");
                        vo.setProgress(0);
                    } else if (now.isAfter(session.getEndTime())) {
                        vo.setStatus("ended");
                        vo.setProgress(100);
                    } else {
                        vo.setStatus("ongoing");
                        long total = java.time.Duration.between(session.getStartTime(), session.getEndTime()).toSeconds();
                        long elapsed = java.time.Duration.between(session.getStartTime(), now).toSeconds();
                        vo.setProgress((int) (elapsed * 100 / total));
                    }

                    int productCount = flashProductMapper.selectCount(
                        new LambdaQueryWrapper<FlashSaleProduct>()
                            .eq(FlashSaleProduct::getSessionId, session.getSessionId())
                    ).intValue();
                    vo.setProductCount(productCount);

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashSaleProductVO> getSessionProducts(Long sessionId, Integer page, Integer pageSize) {
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;

        LambdaQueryWrapper<FlashSaleProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleProduct::getSessionId, sessionId)
               .orderByDesc(FlashSaleProduct::getSortOrder)
               .last("LIMIT " + limit + " OFFSET " + offset);

        List<FlashSaleProduct> products = flashProductMapper.selectList(wrapper);
        FlashSaleSession session = flashSaleSessionMapper.selectById(sessionId);

        return products.stream()
                .map(fp -> convertToFlashSaleProductVO(fp, session))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canBuy(Long userId, Long productId, Long sessionId) {
        FlashSaleProduct flashProduct = flashProductMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProduct>()
                        .eq(FlashSaleProduct::getProductId, productId)
                        .eq(FlashSaleProduct::getSessionId, sessionId)
        );

        if (flashProduct == null || flashProduct.getStockCount() <= flashProduct.getSoldCount()) {
            return false;
        }

        int userBuyCount = getUserBuyCount(userId, sessionId);
        int limitPerUser = flashProduct.getLimitPerUser() != null ? flashProduct.getLimitPerUser() : 1;
        return userBuyCount < limitPerUser;
    }

    @Override
    public int getUserBuyCount(Long userId, Long sessionId) {
        return flashSaleOrderExtMapper.selectCount(
                new LambdaQueryWrapper<FlashSaleOrderExt>()
                        .eq(FlashSaleOrderExt::getUserId, userId)
                        .eq(FlashSaleOrderExt::getSessionId, sessionId)
        ).intValue();
    }

    @Override
    public FlashSaleProductVO getFlashSaleProductDetail(Long productId) {
        // 查询当前进行中的场次
        FlashSaleSession currentSession = getCurrentSession();
        if (currentSession == null) {
            return null;
        }

        FlashSaleProduct flashProduct = flashProductMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProduct>()
                        .eq(FlashSaleProduct::getProductId, productId)
                        .eq(FlashSaleProduct::getSessionId, currentSession.getSessionId())
        );

        if (flashProduct == null) {
            return null;
        }

        return convertToFlashSaleProductVO(flashProduct, currentSession);
    }

    // ========== 前端秒杀页面所需方法 ==========

    @Override
    public boolean checkSeckillEligibility(Long userId, Long productId) {
        FlashSaleSession currentSession = getCurrentSession();
        if (currentSession == null) {
            return false;
        }

        FlashSaleProduct flashProduct = flashProductMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProduct>()
                        .eq(FlashSaleProduct::getProductId, productId)
                        .eq(FlashSaleProduct::getSessionId, currentSession.getSessionId())
                        .eq(FlashSaleProduct::getDeleted, 0)
        );

        if (flashProduct == null || flashProduct.getStockCount() <= flashProduct.getSoldCount()) {
            return false;
        }

        int limitPerUser = flashProduct.getLimitPerUser() != null ? flashProduct.getLimitPerUser() : 1;
        int userBuyCount = getUserBuyCount(userId, currentSession.getSessionId());
        return userBuyCount < limitPerUser;
    }

    @Override
    public String getCannotBuyReason(Long userId, Long productId) {
        FlashSaleSession currentSession = getCurrentSession();
        if (currentSession == null) {
            return "暂无秒杀场次";
        }

        FlashSaleProduct flashProduct = flashProductMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProduct>()
                        .eq(FlashSaleProduct::getProductId, productId)
                        .eq(FlashSaleProduct::getSessionId, currentSession.getSessionId())
                        .eq(FlashSaleProduct::getDeleted, 0)
        );

        if (flashProduct == null) {
            return "商品未参与秒杀";
        }

        if (flashProduct.getStockCount() <= flashProduct.getSoldCount()) {
            return "已抢光";
        }

        int limitPerUser = flashProduct.getLimitPerUser() != null ? flashProduct.getLimitPerUser() : 1;
        int userBuyCount = getUserBuyCount(userId, currentSession.getSessionId());
        if (userBuyCount >= limitPerUser) {
            return "已达限购数量";
        }

        return "无法购买";
    }

    @Override
    public Integer getRemainingStock(Long productId) {
        FlashSaleSession currentSession = getCurrentSession();
        if (currentSession == null) {
            return 0;
        }

        FlashSaleProduct flashProduct = flashProductMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProduct>()
                        .eq(FlashSaleProduct::getProductId, productId)
                        .eq(FlashSaleProduct::getSessionId, currentSession.getSessionId())
                        .eq(FlashSaleProduct::getDeleted, 0)
        );

        if (flashProduct == null) {
            return 0;
        }

        return Math.max(0, flashProduct.getStockCount() - flashProduct.getSoldCount());
    }

    @Override
    public Integer getUserBuyLimit(Long productId) {
        FlashSaleSession currentSession = getCurrentSession();
        if (currentSession == null) {
            return 1;
        }

        FlashSaleProduct flashProduct = flashProductMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProduct>()
                        .eq(FlashSaleProduct::getProductId, productId)
                        .eq(FlashSaleProduct::getSessionId, currentSession.getSessionId())
        );

        return flashProduct != null && flashProduct.getLimitPerUser() != null
                ? flashProduct.getLimitPerUser() : 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> seckillBuy(Long userId, Long productId, Integer quantity, String remark) {
        FlashSaleSession currentSession = getCurrentSession();
        if (currentSession == null) {
            throw new BusinessException("暂无秒杀场次");
        }

        FlashSaleProduct flashProduct = flashProductMapper.selectOne(
                new LambdaQueryWrapper<FlashSaleProduct>()
                        .eq(FlashSaleProduct::getProductId, productId)
                        .eq(FlashSaleProduct::getSessionId, currentSession.getSessionId())
                        .eq(FlashSaleProduct::getDeleted, 0)
        );

        if (flashProduct == null) {
            throw new BusinessException("商品未参与秒杀");
        }

        int remainingStock = flashProduct.getStockCount() - flashProduct.getSoldCount();
        if (remainingStock < quantity) {
            throw new BusinessException("库存不足");
        }

        int limitPerUser = flashProduct.getLimitPerUser() != null ? flashProduct.getLimitPerUser() : 1;
        int userBuyCount = getUserBuyCount(userId, currentSession.getSessionId());
        if (userBuyCount + quantity > limitPerUser) {
            throw new BusinessException("超过限购数量");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 创建订单
        BigDecimal totalAmount = flashProduct.getFlashPrice().multiply(BigDecimal.valueOf(quantity));

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductId(productId);
        order.setBuyerId(userId);
        order.setSellerId(product.getSellerId());
        order.setType(1);
        order.setOrderType(1);
        order.setAmount(totalAmount);
        order.setStatus(0);
        order.setRemark(remark);

        orderMapper.insert(order);

        // 创建秒杀订单扩展记录
        FlashSaleOrderExt flashOrderExt = new FlashSaleOrderExt();
        flashOrderExt.setOrderId(order.getOrderId());
        flashOrderExt.setUserId(userId);
        flashOrderExt.setProductId(productId);
        flashOrderExt.setSessionId(currentSession.getSessionId());
        flashOrderExt.setFlashPrice(flashProduct.getFlashPrice());

        BigDecimal discount = flashProduct.getFlashPrice()
                .divide(product.getPrice(), 1, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.TEN);
        flashOrderExt.setDiscount(discount);
        flashOrderExt.setSeckillTime(LocalDateTime.now());

        flashSaleOrderExtMapper.insert(flashOrderExt);

        // 更新已售数量
        flashProduct.setSoldCount(flashProduct.getSoldCount() + quantity);
        flashProductMapper.updateById(flashProduct);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("orderNo", order.getOrderNo());
        result.put("sessionId", currentSession.getSessionId());
        result.put("productId", productId);
        result.put("quantity", quantity);
        result.put("totalAmount", totalAmount);

        log.info("秒杀抢购成功, userId={}, productId={}, sessionId={}, orderId={}",
                 userId, productId, currentSession.getSessionId(), order.getOrderId());

        return result;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 10000);
        return String.format("SEKK%s%04d", timestamp, random);
    }
}
