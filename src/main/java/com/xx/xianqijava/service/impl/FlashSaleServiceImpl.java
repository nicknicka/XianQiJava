package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.FlashSaleProduct;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.entity.FlashSaleOrderExt;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.FlashSaleOrderExtMapper;
import com.xx.xianqijava.mapper.FlashSaleProductMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.service.FlashSaleService;
import com.xx.xianqijava.util.ProductConditionUtil;
import com.xx.xianqijava.vo.FlashSaleProductVO;
import com.xx.xianqijava.vo.FlashSaleSessionVO;
import com.xx.xianqijava.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        // 查询当前进行中的场次（只查询启用的）
        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(FlashSaleSession::getStartTime, now)
               .ge(FlashSaleSession::getEndTime, now)
               .eq(FlashSaleSession::getEnabled, 1)  // 只查询启用的场次
               .orderByAsc(FlashSaleSession::getStartTime)
               .last("LIMIT 1");

        return flashSaleSessionMapper.selectOne(wrapper);
    }

    @Override
    public List<ProductVO> getCurrentFlashSaleProducts(Integer limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        // 查询当天的所有场次（只查询启用的）
        LambdaQueryWrapper<FlashSaleSession> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.ge(FlashSaleSession::getStartTime, startOfDay)
                      .le(FlashSaleSession::getStartTime, endOfDay)
                      .eq(FlashSaleSession::getEnabled, 1)  // 只查询启用的场次
                      .orderByAsc(FlashSaleSession::getStartTime);

        List<FlashSaleSession> todaySessions = flashSaleSessionMapper.selectList(sessionWrapper);

        if (todaySessions.isEmpty()) {
            return List.of();
        }

        // 获取所有场次的ID
        List<Long> sessionIds = todaySessions.stream()
                .map(FlashSaleSession::getSessionId)
                .collect(Collectors.toList());

        // 查询这些场次的商品
        LambdaQueryWrapper<FlashSaleProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FlashSaleProduct::getSessionId, sessionIds)
               .orderByDesc(FlashSaleProduct::getSortOrder);

        // 如果有limit限制，只取前N个
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        List<FlashSaleProduct> flashProducts = flashProductMapper.selectList(wrapper);

        // 转换为 ProductVO，包含秒杀价
        // 过滤掉 null 值（商品已被删除或不存在的情况）
        return flashProducts.stream()
                .map(this::convertToProductVO)
                .filter(vo -> vo != null)
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

        // 设置商品ID（兼容前端使用的 id 字段）
        vo.setId(product.getProductId());

        // 设置卖家信息兼容字段
        vo.setUserId(product.getSellerId());
        vo.setUserName(vo.getSellerNickname());
        vo.setUserAvatar(vo.getSellerAvatar());
        vo.setCreditLevel(vo.getSellerCreditScore());

        // 设置秒杀价格（同时设置两个字段以兼容不同命名）
        vo.setFlashPrice(flashProduct.getFlashPrice());
        vo.setSeckillPrice(flashProduct.getFlashPrice());
        vo.setIsFlashSale(true);

        // 设置秒杀库存信息
        vo.setFlashSaleStock(flashProduct.getStockCount());
        vo.setStock(flashProduct.getStockCount());  // 兼容字段
        vo.setFlashSaleSold(flashProduct.getSoldCount());
        vo.setLimitPerUser(flashProduct.getLimitPerUser() != null ? flashProduct.getLimitPerUser() : 1);

        // 设置成色兼容字段
        vo.setCondition(ProductConditionUtil.levelToString(vo.getConditionLevel()));

        // 计算已抢百分比
        if (flashProduct.getStockCount() > 0) {
            vo.setSoldPercent((int) ((long) flashProduct.getSoldCount() * 100 / flashProduct.getStockCount()));
        }

        // 计算折扣
        if (flashProduct.getFlashPrice().compareTo(BigDecimal.ZERO) > 0 && product.getPrice() != null) {
            BigDecimal discount = flashProduct.getFlashPrice()
                    .divide(product.getPrice(), 1, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN);
            vo.setDiscount(discount.intValue());
        }

        FlashSaleSession session = flashSaleSessionMapper.selectById(flashProduct.getSessionId());
        if (session != null) {
            vo.setSessionId(session.getSessionId());
            vo.setEndTime(session.getEndTime().toString());
            vo.setStartTime(session.getStartTime().toString());

            // 计算场次状态
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(session.getStartTime())) {
                vo.setSessionStatus("upcoming");
            } else if (now.isAfter(session.getEndTime())) {
                vo.setSessionStatus("ended");
            } else {
                vo.setSessionStatus("ongoing");
            }
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

        // 计算已抢百分比（避免整数除法精度丢失）
        if (flashProduct.getStockCount() > 0) {
            vo.setSoldPercent((int) ((long) flashProduct.getSoldCount() * 100 / flashProduct.getStockCount()));
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
                        .divide(product.getPrice(), 1, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.TEN);
                vo.setDiscount(discount.intValue());
            }

            vo.setLocation(product.getLocation());
            vo.setCondition(product.getConditionLevel() != null ? product.getConditionLevel().toString() : "");
            vo.setCategoryId(product.getCategoryId() != null ? product.getCategoryId().intValue() : null);
        }

        // 设置状态（根据场次时间计算）
        if (session != null) {
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
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        // 查询当天所有场次（只查询启用的）
        LambdaQueryWrapper<FlashSaleSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FlashSaleSession::getStartTime, startOfDay)
               .le(FlashSaleSession::getStartTime, endOfDay)
               .eq(FlashSaleSession::getEnabled, 1)  // 只查询启用的场次
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

                    // 不再计算状态和进度，由前端根据时间计算

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
                .divide(product.getPrice(), 1, RoundingMode.HALF_UP)
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
