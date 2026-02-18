package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.ProductViewHistory;
import com.xx.xianqijava.service.ProductViewHistoryService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 商品浏览历史控制器
 */
@Slf4j
@Tag(name = "浏览历史管理")
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductViewHistoryService productViewHistoryService;

    /**
     * 浏览历史列表
     */
    @Operation(summary = "浏览历史列表")
    @GetMapping
    public Result<IPage<ProductVO>> getViewHistoryList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询浏览历史, userId={}, page={}, size={}", userId, page, size);

        Page<ProductViewHistory> pageParam = new Page<>(page, size);
        IPage<ProductVO> productPage = productViewHistoryService.getViewHistoryList(userId, pageParam);

        return Result.success(productPage);
    }

    /**
     * 删除浏览记录
     */
    @Operation(summary = "删除浏览记录")
    @DeleteMapping("/{historyId}")
    public Result<Void> removeViewHistory(
            @Parameter(description = "浏览记录ID") @PathVariable("historyId") Long historyId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除浏览记录, userId={}, historyId={}", userId, historyId);
        productViewHistoryService.removeViewHistory(userId, historyId);
        return Result.success("删除成功");
    }

    /**
     * 清空浏览历史
     */
    @Operation(summary = "清空浏览历史")
    @DeleteMapping("/clear")
    public Result<Void> clearViewHistory() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("清空浏览历史, userId={}", userId);
        productViewHistoryService.clearViewHistory(userId);
        return Result.success("清空成功");
    }
}
