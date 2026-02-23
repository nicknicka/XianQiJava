package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 获取分类列表
     */
    @Operation(summary = "获取分类列表")
    @GetMapping
    public Result<List<Category>> getCategoryList() {
        log.info("获取分类列表");

        List<Category> categories = categoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSortOrder)
        );

        return Result.success(categories);
    }

    /**
     * 获取分类详情
     */
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getCategoryDetail(
            @PathVariable("id") Long id) {
        log.info("获取分类详情, categoryId={}", id);

        Category category = categoryMapper.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            return Result.error("分类不存在");
        }

        return Result.success(category);
    }
}
