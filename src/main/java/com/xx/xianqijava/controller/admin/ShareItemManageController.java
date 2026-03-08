package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.ShareItemManageQueryDTO;
import com.xx.xianqijava.dto.admin.ShareItemStatusUpdateDTO;
import com.xx.xianqijava.service.ShareItemManageService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.admin.ShareItemManageStatistics;
import com.xx.xianqijava.vo.admin.ShareItemManageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 共享物品管理控制器 - 管理端
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/share-item")
@Tag(name = "共享物品管理", description = "共享物品管理相关接口")
@SecurityRequirement(name = "bearer-auth")
public class ShareItemManageController {

    private final ShareItemManageService shareItemManageService;

    /**
     * 分页查询共享物品列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询共享物品列表", description = "支持多种条件筛选和排序")
    public Page<ShareItemManageVO> getShareItemList(ShareItemManageQueryDTO queryDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}查询共享物品列表，查询条件：{}", adminId, queryDTO);
        return shareItemManageService.getShareItemList(queryDTO);
    }

    /**
     * 获取共享物品详情
     */
    @GetMapping("/{shareId}")
    @Operation(summary = "获取共享物品详情", description = "根据共享物品ID获取详细信息")
    public ShareItemManageVO getShareItemDetail(@PathVariable Long shareId) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取共享物品详情，共享物品ID：{}", adminId, shareId);
        return shareItemManageService.getShareItemDetail(shareId);
    }

    /**
     * 更新共享物品状态
     */
    @PutMapping("/status")
    @Operation(summary = "更新共享物品状态", description = "下架/上架/设置为借用中")
    public Boolean updateShareItemStatus(@Valid @RequestBody ShareItemStatusUpdateDTO updateDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}更新共享物品状态，共享物品ID：{}，状态：{}",
                adminId, updateDTO.getShareId(), updateDTO.getStatus());
        return shareItemManageService.updateShareItemStatus(updateDTO);
    }

    /**
     * 获取共享物品统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取共享物品统计信息", description = "获取物品总数、借用次数、押金金额等统计数据")
    public ShareItemManageStatistics getShareItemStatistics() {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取共享物品统计信息", adminId);
        return shareItemManageService.getShareItemStatistics();
    }
}
