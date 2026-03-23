package com.xx.xianqijava.agent.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.xianqijava.entity.Product;
import com.xx.xianqijava.service.ProductService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 商品相关工具集
 *
 * @author Claude
 * @since 2026-03-23
 */
@Service
public class ProductTools {

    private static final Logger log = LoggerFactory.getLogger(ProductTools.class);

    @Resource
    private ProductService productService;

    /**
     * 搜索商品
     *
     * @param keyword    关键词
     * @param categoryId 分类ID（可选）
     * @param maxPrice   最高价格（可选）
     * @return 商品列表
     */
    @Tool("搜索商品，支持关键词、分类、价格筛选")
    public String searchProducts(
            @P("搜索关键词") String keyword,
            @P("分类ID（可选）") Long categoryId,
            @P("最高价格（可选）") Double maxPrice
    ) {
        log.info("执行工具：searchProducts，keyword={}, categoryId={}, maxPrice={}",
                keyword, categoryId, maxPrice);

        try {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();

            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like("title", keyword)
                        .or()
                        .like("description", keyword));
            }

            // 分类筛选
            if (categoryId != null) {
                wrapper.eq("category_id", categoryId);
            }

            // 价格筛选
            if (maxPrice != null) {
                wrapper.le("price", maxPrice);
            }

            // 只显示上架商品
            wrapper.eq("status", 1);

            // 限制数量
            wrapper.last("LIMIT 10");

            List<Product> products = productService.list(wrapper);

            if (products.isEmpty()) {
                return "未找到符合条件的商品";
            }

            StringBuilder result = new StringBuilder();
            result.append("找到 ").append(products.size()).append(" 件商品：\n\n");

            for (Product p : products) {
                String conditionText = switch (p.getConditionLevel() != null ? p.getConditionLevel() : 5) {
                    case 10 -> "全新";
                    case 9 -> "几乎全新";
                    case 8 -> "很新";
                    case 7 -> "良好";
                    case 6 -> "一般";
                    default -> "旧";
                };

                result.append(String.format("""
                        - %s
                          价格：¥%.2f
                          成色：%s
                          浏览：%d次
                          商品ID：%d
                        """,
                        p.getTitle(),
                        p.getPrice(),
                        conditionText,
                        p.getViewCount() != null ? p.getViewCount() : 0,
                        p.getProductId()
                ));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索商品失败", e);
            return "搜索商品失败：" + e.getMessage();
        }
    }

    /**
     * 获取热门商品
     *
     * @param limit 数量限制
     * @return 热门商品列表
     */
    @Tool("获取热门商品，按浏览量和收藏量排序")
    public String getHotProducts(@P("数量") Integer limit) {
        log.info("执行工具：getHotProducts，limit={}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 5;
            }

            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1); // 上架状态
            wrapper.orderByDesc("view_count"); // 按浏览量排序
            wrapper.last("LIMIT " + limit);

            List<Product> products = productService.list(wrapper);

            if (products.isEmpty()) {
                return "暂无热门商品";
            }

            StringBuilder result = new StringBuilder();
            result.append("热门商品推荐（TOP ").append(products.size()).append("）：\n\n");

            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                String conditionText = switch (p.getConditionLevel() != null ? p.getConditionLevel() : 5) {
                    case 10 -> "全新";
                    case 9 -> "几乎全新";
                    case 8 -> "很新";
                    case 7 -> "良好";
                    case 6 -> "一般";
                    default -> "旧";
                };

                result.append(String.format("""
                        %d. %s
                           价格：¥%.2f
                           浏览：%d次
                           成色：%s
                           商品ID：%d
                        """,
                        i + 1,
                        p.getTitle(),
                        p.getPrice(),
                        p.getViewCount() != null ? p.getViewCount() : 0,
                        conditionText,
                        p.getProductId()
                ));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取热门商品失败", e);
            return "获取热门商品失败：" + e.getMessage();
        }
    }

    /**
     * 获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @Tool("获取商品详细信息")
    public String getProductDetail(@P("商品ID") Long productId) {
        log.info("执行工具：getProductDetail，productId={}", productId);

        try {
            Product product = productService.getById(productId);
            if (product == null) {
                return "商品不存在";
            }

            String conditionText = switch (product.getConditionLevel() != null ? product.getConditionLevel() : 5) {
                case 10 -> "全新";
                case 9 -> "几乎全新";
                case 8 -> "很新";
                case 7 -> "良好";
                case 6 -> "一般";
                default -> "旧";
            };

            String statusText = switch (product.getStatus()) {
                case 0 -> "已下架";
                case 1 -> "在售";
                case 2 -> "已售出";
                case 3 -> "已预订";
                case 4 -> "草稿";
                default -> "未知";
            };

            return String.format("""
                    商品详情：
                    - 商品名称：%s
                    - 价格：¥%.2f
                    - 原价：¥%.2f
                    - 成色：%s
                    - 分类ID：%d
                    - 描述：%s
                    - 卖家ID：%d
                    - 状态：%s
                    - 浏览次数：%d
                    """,
                    product.getTitle(),
                    product.getPrice(),
                    product.getOriginalPrice(),
                    conditionText,
                    product.getCategoryId(),
                    product.getDescription() != null ?
                        (product.getDescription().length() > 100 ?
                            product.getDescription().substring(0, 100) + "..." :
                            product.getDescription()) : "无描述",
                    product.getSellerId(),
                    statusText,
                    product.getViewCount() != null ? product.getViewCount() : 0
            );

        } catch (Exception e) {
            log.error("获取商品详情失败：productId={}", productId, e);
            return "获取商品详情失败：" + e.getMessage();
        }
    }
}
