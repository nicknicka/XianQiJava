package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.xianqijava.annotation.OperationLog;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.util.IdConverter;
import com.xx.xianqijava.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类管理后台接口
 */
@Slf4j
@Tag(name = "分类管理后台")
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryManageController {

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

    // ========== 查询接口 ==========

    /**
     * 获取分类列表（树形结构）
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    public Result<List<CategoryVO>> getCategoryTree() {
        log.info("获取分类树");

        List<Category> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSortOrder)
        );

        List<CategoryVO> categoryVOList = categories.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        // 构建树形结构
        List<CategoryVO> tree = buildTree(categoryVOList, null);

        return Result.success(tree);
    }

    /**
     * 获取分类列表（平铺结构）
     */
    @GetMapping("/list")
    @Operation(summary = "获取分类列表")
    public Result<List<CategoryVO>> getCategoryList() {
        log.info("获取分类列表");

        List<Category> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSortOrder)
        );

        List<CategoryVO> categoryVOList = categories.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return Result.success(categoryVOList);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "获取分类详情")
    public Result<CategoryVO> getCategoryDetail(
            @Parameter(description = "分类ID") @PathVariable Long categoryId) {
        log.info("获取分类详情, categoryId={}", categoryId);

        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            return Result.error("分类不存在");
        }

        CategoryVO vo = convertToVO(category);
        return Result.success(vo);
    }

    // ========== 增删改接口 ==========

    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类")
    @OperationLog(
            module = "category",
            action = "create",
            description = "创建分类"
    )
    public Result<Long> createCategory(@RequestBody Category category) {
        log.info("创建分类: {}", category.getName());

        // 验证必填字段
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return Result.error("分类名称不能为空");
        }

        // 检查名称是否重复
        Long count = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName())
                .eq(Category::getDeleted, 0)
        );
        if (count > 0) {
            return Result.error("分类名称已存在");
        }

        // 设置默认值
        if (category.getStatus() == null) {
            category.setStatus(1); // 默认启用
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(99);
        }
        if (category.getParentId() == null) {
            category.setParentId(0L); // 0表示顶级分类
        }
        category.setDeleted(0);
        category.setCreateTime(java.time.LocalDateTime.now());

        categoryMapper.insert(category);
        log.info("创建分类成功, categoryId={}", category.getCategoryId());

        return Result.success(category.getCategoryId());
    }

    /**
     * 更新分类
     */
    @PutMapping
    @Operation(summary = "更新分类")
    @OperationLog(
            module = "category",
            action = "update",
            description = "更新分类"
    )
    public Result<Boolean> updateCategory(@RequestBody Category category) {
        log.info("更新分类: categoryId={}", category.getCategoryId());

        if (category.getCategoryId() == null) {
            return Result.error("分类ID不能为空");
        }

        Category existing = categoryMapper.selectById(category.getCategoryId());
        if (existing == null || existing.getDeleted() == 1) {
            return Result.error("分类不存在");
        }

        // 检查名称是否重复
        if (category.getName() != null && !category.getName().equals(existing.getName())) {
            Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                    .eq(Category::getName, category.getName())
                    .eq(Category::getDeleted, 0)
                    .ne(Category::getCategoryId, category.getCategoryId())
            );
            if (count > 0) {
                return Result.error("分类名称已存在");
            }
        }

        category.setUpdateTime(java.time.LocalDateTime.now());
        categoryMapper.updateById(category);

        log.info("更新分类成功, categoryId={}", category.getCategoryId());
        return Result.success(true);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "删除分类")
    @OperationLog(
            module = "category",
            action = "delete",
            description = "删除分类"
    )
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId) {
        log.info("删除分类, categoryId={}", categoryId);

        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            return Result.error("分类不存在");
        }

        // 检查是否有子分类
        Long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId)
                .eq(Category::getDeleted, 0)
        );
        if (childCount > 0) {
            return Result.error("该分类下有子分类，无法删除");
        }

        // 逻辑删除
        category.setDeleted(1);
        category.setUpdateTime(java.time.LocalDateTime.now());
        categoryMapper.updateById(category);

        log.info("删除分类成功, categoryId={}", categoryId);
        return Result.success(true);
    }

    /**
     * 更新分类状态
     */
    @PutMapping("/{categoryId}/status")
    @Operation(summary = "更新分类状态")
    @OperationLog(
            module = "category",
            action = "update_status",
            description = "更新分类状态"
    )
    public Result<Boolean> updateCategoryStatus(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            @Parameter(description = "状态（0-禁用，1-启用）") @RequestParam Integer status) {
        log.info("更新分类状态, categoryId={}, status={}", categoryId, status);

        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            return Result.error("分类不存在");
        }

        category.setStatus(status);
        category.setUpdateTime(java.time.LocalDateTime.now());
        categoryMapper.updateById(category);

        log.info("更新分类状态成功, categoryId={}, status={}", categoryId, status);
        return Result.success(true);
    }

    /**
     * 批量更新排序
     */
    @PutMapping("/sort")
    @Operation(summary = "批量更新排序")
    @OperationLog(
            module = "category",
            action = "batch_update_sort",
            description = "批量更新排序"
    )
    public Result<Boolean> batchUpdateSortOrder(@RequestBody List<Map<String, Integer>> sortData) {
        log.info("批量更新排序, 数量={}", sortData.size());

        for (Map<String, Integer> item : sortData) {
            Integer categoryId = item.get("categoryId");
            Integer sortOrder = item.get("sortOrder");

            if (categoryId != null && sortOrder != null) {
                categoryMapper.update(null,
                    new LambdaUpdateWrapper<Category>()
                        .eq(Category::getCategoryId, categoryId)
                        .set(Category::getSortOrder, sortOrder)
                );
            }
        }

        log.info("批量更新排序成功");
        return Result.success(true);
    }

    // ========== 辅助方法 ==========

    /**
     * 构建树形结构
     */
    private List<CategoryVO> buildTree(List<CategoryVO> allNodes, Long parentId) {
        List<CategoryVO> tree = new ArrayList<>();

        for (CategoryVO node : allNodes) {
            Long nodeParentId = IdConverter.toLong(node.getParentId());
            if (nodeParentId == null) {
                nodeParentId = 0L;
            }

            if ((parentId == null && nodeParentId == 0) || (parentId != null && nodeParentId.equals(parentId))) {
                // 递归查找子节点
                List<CategoryVO> children = buildTree(allNodes, IdConverter.toLong(node.getCategoryId()));
                if (!children.isEmpty()) {
                    node.setChildren(children);
                }
                tree.add(node);
            }
        }

        return tree;
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
        vo.setParentId(String.valueOf(category.getParentId()));
        vo.setCreateTime(category.getCreateTime() != null ?
            category.getCreateTime().toString() : null);
        vo.setUpdateTime(category.getUpdateTime() != null ?
            category.getUpdateTime().toString() : null);

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
