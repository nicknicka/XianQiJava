package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.ShareItemCreateDTO;
import com.xx.xianqijava.dto.ShareItemDraftSaveDTO;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.service.ShareItemService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.ShareItemDraftVO;
import com.xx.xianqijava.vo.ShareItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 共享物品控制器
 */
@Slf4j
@Tag(name = "共享物品管理")
@RestController
@RequestMapping("/share-item")
@RequiredArgsConstructor
public class ShareItemController {

    private final ShareItemService shareItemService;

    /**
     * 创建共享物品
     */
    @PostMapping
    @Operation(summary = "创建共享物品")
    public Result<ShareItemVO> createShareItem(@Valid @RequestBody ShareItemCreateDTO createDTO) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建共享物品, ownerId={}, title={}", ownerId, createDTO.getTitle());
        ShareItemVO shareItemVO = shareItemService.createShareItem(createDTO, ownerId);
        return Result.success("共享物品发布成功", shareItemVO);
    }

    /**
     * 更新共享物品
     */
    @PutMapping("/{shareId}")
    @Operation(summary = "更新共享物品")
    public Result<ShareItemVO> updateShareItem(
            @Parameter(description = "共享物品ID") @PathVariable("shareId") Long shareId,
            @Valid @RequestBody ShareItemCreateDTO createDTO) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新共享物品, shareId={}, ownerId={}", shareId, ownerId);
        ShareItemVO shareItemVO = shareItemService.updateShareItem(shareId, createDTO, ownerId);
        return Result.success("共享物品更新成功", shareItemVO);
    }

    /**
     * 删除共享物品
     */
    @DeleteMapping("/{shareId}")
    @Operation(summary = "删除共享物品")
    public Result<Void> deleteShareItem(
            @Parameter(description = "共享物品ID") @PathVariable("shareId") Long shareId) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除共享物品, shareId={}, ownerId={}", shareId, ownerId);
        shareItemService.deleteShareItem(shareId, ownerId);
        return Result.success("共享物品删除成功");
    }

    /**
     * 更新共享物品状态
     */
    @PutMapping("/{shareId}/status")
    @Operation(summary = "更新共享物品状态")
    public Result<Void> updateShareItemStatus(
            @Parameter(description = "共享物品ID") @PathVariable("shareId") Long shareId,
            @Parameter(description = "状态：0-下架，1-可借用") @RequestParam("status") Integer status) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新共享物品状态, shareId={}, status={}, ownerId={}", shareId, status, ownerId);
        shareItemService.updateShareItemStatus(shareId, status, ownerId);
        return Result.success("状态更新成功");
    }

    /**
     * 获取共享物品详情
     */
    @GetMapping("/{shareId}")
    @Operation(summary = "获取共享物品详情")
    public Result<ShareItemVO> getShareItemDetail(
            @Parameter(description = "共享物品ID") @PathVariable("shareId") Long shareId) {
        log.info("查询共享物品详情, shareId={}", shareId);
        ShareItemVO shareItemVO = shareItemService.getShareItemDetail(shareId);
        return Result.success(shareItemVO);
    }

    /**
     * 获取共享物品列表
     */
    @GetMapping
    @Operation(summary = "获取共享物品列表")
    public Result<IPage<ShareItemVO>> getShareItemList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        log.info("查询共享物品列表, page={}, size={}, categoryId={}, status={}, keyword={}",
                page, size, categoryId, status, keyword);

        Page<ShareItem> pageParam = new Page<>(page, size);
        IPage<ShareItemVO> shareItemPage = shareItemService.getShareItemList(pageParam, categoryId, status, keyword);

        return Result.success(shareItemPage);
    }

    /**
     * 获取我的共享物品列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的共享物品列表")
    public Result<IPage<ShareItemVO>> getMyShareItems(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("查询我的共享物品列表, ownerId={}, page={}, size={}", ownerId, page, size);

        Page<ShareItem> pageParam = new Page<>(page, size);
        IPage<ShareItemVO> shareItemPage = shareItemService.getMyShareItems(pageParam, ownerId);

        return Result.success(shareItemPage);
    }

    /**
     * 获取附近共享物品列表
     */
    @GetMapping("/nearby")
    @Operation(summary = "获取附近共享物品列表")
    public Result<IPage<ShareItemVO>> getNearbyShareItems(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取附近共享物品列表, userId={}, page={}, size={}", userId, page, size);

        Page<ShareItem> pageParam = new Page<>(page, size);
        IPage<ShareItemVO> shareItemPage = shareItemService.getNearbyShareItems(pageParam, userId);

        return Result.success(shareItemPage);
    }

    // ==================== 草稿相关接口 ====================

    /**
     * 保存共享物品草稿
     */
    @Operation(summary = "保存共享物品草稿")
    @PostMapping("/draft")
    public Result<ShareItemDraftVO> saveDraft(@Valid @RequestBody ShareItemDraftSaveDTO draftDTO) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("保存共享物品草稿, ownerId={}", ownerId);
        ShareItemDraftVO draftVO = shareItemService.saveDraft(draftDTO, ownerId);
        return Result.success("草稿保存成功", draftVO);
    }

    /**
     * 获取草稿列表
     */
    @Operation(summary = "获取草稿列表")
    @GetMapping("/draft")
    public Result<IPage<ShareItemDraftVO>> getDraftList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer size) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取草稿列表, ownerId={}, page={}, size={}", ownerId, page, size);

        Page<ShareItem> pageParam = new Page<>(page, size);
        IPage<ShareItemDraftVO> draftPage = shareItemService.getDraftList(pageParam, ownerId);

        return Result.success(draftPage);
    }

    /**
     * 获取草稿详情
     */
    @Operation(summary = "获取草稿详情")
    @GetMapping("/draft/{id}")
    public Result<ShareItemDraftVO> getDraftDetail(
            @Parameter(description = "草稿ID") @PathVariable("id") Long id) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取草稿详情, draftId={}, ownerId={}", id, ownerId);
        ShareItemDraftVO draftVO = shareItemService.getDraftDetail(id, ownerId);
        return Result.success(draftVO);
    }

    /**
     * 从草稿发布共享物品
     */
    @Operation(summary = "从草稿发布共享物品")
    @PostMapping("/draft/{id}/publish")
    public Result<ShareItemVO> publishFromDraft(
            @Parameter(description = "草稿ID") @PathVariable("id") Long id) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("从草稿发布共享物品, draftId={}, ownerId={}", id, ownerId);
        ShareItemVO shareItemVO = shareItemService.publishFromDraft(id, ownerId);
        return Result.success("共享物品发布成功", shareItemVO);
    }

    /**
     * 删除草稿
     */
    @Operation(summary = "删除草稿")
    @DeleteMapping("/draft/{id}")
    public Result<Void> deleteDraft(
            @Parameter(description = "草稿ID") @PathVariable("id") Long id) {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除草稿, draftId={}, ownerId={}", id, ownerId);
        shareItemService.deleteDraft(id, ownerId);
        return Result.success("草稿删除成功");
    }

    /**
     * 获取草稿数量
     */
    @Operation(summary = "获取草稿数量")
    @GetMapping("/draft/count")
    public Result<java.util.Map<String, Object>> getDraftCount() {
        Long ownerId = SecurityUtil.getCurrentUserIdRequired();
        int count = shareItemService.countUserDrafts(ownerId);
        return Result.success(java.util.Map.of("count", count, "maxCount", 10));
    }
}
