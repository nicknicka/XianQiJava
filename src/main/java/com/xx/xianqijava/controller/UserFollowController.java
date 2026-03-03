package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.UserFollow;
import com.xx.xianqijava.service.UserFollowService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注控制器
 */
@Slf4j
@Tag(name = "用户关注")
@RestController
@RequestMapping("/user/follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    /**
     * 关注用户
     */
    @Operation(summary = "关注用户")
    @PostMapping("/{followingId}")
    public Result<Void> followUser(
            @Parameter(description = "被关注用户ID") @PathVariable("followingId") Long followingId) {
        Long followerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("关注用户请求, followerId={}, followingId={}", followerId, followingId);
        userFollowService.followUser(followerId, followingId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注
     */
    @Operation(summary = "取消关注")
    @DeleteMapping("/{followingId}")
    public Result<Void> unfollowUser(
            @Parameter(description = "被关注用户ID") @PathVariable("followingId") Long followingId) {
        Long followerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("取消关注请求, followerId={}, followingId={}", followerId, followingId);
        userFollowService.unfollowUser(followerId, followingId);
        return Result.success("取消关注成功");
    }

    /**
     * 检查是否已关注
     */
    @Operation(summary = "检查是否已关注")
    @GetMapping("/check/{followingId}")
    public Result<Boolean> checkFollowing(
            @Parameter(description = "被关注用户ID") @PathVariable("followingId") Long followingId) {
        Long followerId = SecurityUtil.getCurrentUserIdRequired();
        boolean isFollowing = userFollowService.isFollowing(followerId, followingId);
        return Result.success(isFollowing);
    }

    /**
     * 获取关注列表
     */
    @Operation(summary = "获取关注列表")
    @GetMapping("/following")
    public Result<IPage<UserInfoVO>> getFollowingList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long followerId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取关注列表请求, followerId={}, page={}, size={}", followerId, page, size);

        Page<UserFollow> pageParam = new Page<>(page, size);
        IPage<UserInfoVO> result = userFollowService.getFollowingList(followerId, pageParam);
        return Result.success(result);
    }

    /**
     * 获取粉丝列表
     */
    @Operation(summary = "获取粉丝列表")
    @GetMapping("/followers")
    public Result<IPage<UserInfoVO>> getFollowerList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long followingId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取粉丝列表请求, followingId={}, page={}, size={}", followingId, page, size);

        Page<UserFollow> pageParam = new Page<>(page, size);
        IPage<UserInfoVO> result = userFollowService.getFollowerList(followingId, pageParam);
        return Result.success(result);
    }
}
