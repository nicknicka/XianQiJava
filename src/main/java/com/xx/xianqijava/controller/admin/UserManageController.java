package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.admin.UserQueryDTO;
import com.xx.xianqijava.dto.admin.UserUpdateStatusDTO;
import com.xx.xianqijava.service.UserManageService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.admin.UserManageVO;
import com.xx.xianqijava.vo.admin.UserStatisticsInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器 - 管理端
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
@Tag(name = "用户管理", description = "用户管理相关接口")
@SecurityRequirement(name = "bearer-auth")
public class UserManageController {

    private final UserManageService userManageService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "支持多种条件筛选和排序")
    public Result<Page<UserManageVO>> getUserList(UserQueryDTO queryDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("========================================");
        log.info("📋 管理员{}查询用户列表", adminId);
        log.info("查询参数：pageNum={}, pageSize={}, keyword={}, status={}",
                queryDTO.getPageNum(), queryDTO.getPageSize(),
                queryDTO.getKeyword(), queryDTO.getStatus());

        Page<UserManageVO> result = userManageService.getUserList(queryDTO);

        log.info("查询结果：total={}, records={}, firstRecord={}",
                result.getTotal(), result.getRecords().size(),
                result.getRecords().isEmpty() ? "null" : result.getRecords().get(0).getUsername());
        log.info("========================================");

        return Result.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取详细信息")
    public Result<UserManageVO> getUserDetail(@PathVariable Long userId) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取用户详情，用户ID：{}", adminId, userId);
        return Result.success(userManageService.getUserDetail(userId));
    }

    /**
     * 更新用户状态（封禁/解封）
     */
    @PutMapping("/status")
    @Operation(summary = "更新用户状态", description = "封禁或解封用户，封禁时需填写原因")
    public Result<Boolean> updateUserStatus(@Valid @RequestBody UserUpdateStatusDTO updateDTO) {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}更新用户状态，用户ID：{}，状态：{}",
                adminId, updateDTO.getUserId(), updateDTO.getStatus());
        return Result.success(userManageService.updateUserStatus(updateDTO));
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计信息", description = "获取用户总数、封禁数、新增数等统计数据")
    public Result<UserStatisticsInfo> getUserStatistics() {
        Long adminId = SecurityUtil.getCurrentUserId();
        log.info("管理员{}获取用户统计信息", adminId);
        return Result.success(userManageService.getUserStatistics());
    }
}
