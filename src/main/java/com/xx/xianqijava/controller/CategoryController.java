package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类控制器
 */
@Slf4j
@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;

    /**
     * 默认分类渐变色映射
     */
    private static final Map<String, String> DEFAULT_GRADIENTS = new HashMap<>();
    static {
        DEFAULT_GRADIENTS.put("digital", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        DEFAULT_GRADIENTS.put("books", "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)");
        DEFAULT_GRADIENTS.put("daily", "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)");
        DEFAULT_GRADIENTS.put("clothing", "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)");
        DEFAULT_GRADIENTS.put("sports", "linear-gradient(135deg, #fa709a 0%, #fee140 100%)");
        DEFAULT_GRADIENTS.put("beauty", "linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)");
        DEFAULT_GRADIENTS.put("food", "linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)");
        DEFAULT_GRADIENTS.put("other", "linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)");
    }

    /**
     * 获取分类列表
     */
    @Operation(summary = "获取分类列表")
    @GetMapping
    public Result<List<CategoryVO>> getCategoryList() {
        log.info("获取分类列表");

        List<Category> categories = categoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSortOrder)
        );

        List<CategoryVO> categoryVOList = new ArrayList<>();
        for (Category category : categories) {
            CategoryVO vo = convertToVO(category);
            categoryVOList.add(vo);
        }

        return Result.success(categoryVOList);
    }

    /**
     * 获取分类详情
     */
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryDetail(
            @PathVariable("id") Long id) {
        log.info("获取分类详情, categoryId={}", id);

        Category category = categoryMapper.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            return Result.error("分类不存在");
        }

        CategoryVO vo = convertToVO(category);
        return Result.success(vo);
    }

    /**
     * 转换为 VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setCategoryId(String.valueOf(category.getCategoryId()));
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());

        // 如果数据库中没有 code，根据名称生成默认值
        String code = category.getCode();
        if (code == null || code.isEmpty()) {
            code = generateCode(category.getName());
        }
        vo.setCode(code);

        // 如果数据库中没有 gradient，使用默认值
        String gradient = category.getGradient();
        if (gradient == null || gradient.isEmpty()) {
            gradient = DEFAULT_GRADIENTS.getOrDefault(code, "linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)");
        }
        vo.setGradient(gradient);

        return vo;
    }

    /**
     * 根据分类名称生成 code
     */
    private String generateCode(String name) {
        if (name == null) return "other";

        switch (name) {
            case "数码产品": return "digital";
            case "书籍教材": return "books";
            case "生活用品": return "daily";
            case "服装鞋帽": return "clothing";
            case "运动器材": return "sports";
            case "美妆护肤": return "beauty";
            case "食品零食": return "food";
            default: return "other";
        }
    }
}
