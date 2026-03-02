package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.HotTag;
import com.xx.xianqijava.entity.SystemNotification;
import com.xx.xianqijava.mapper.HotTagMapper;
import com.xx.xianqijava.mapper.SystemNotificationMapper;
import com.xx.xianqijava.vo.HotTagVO;
import com.xx.xianqijava.vo.SystemNotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公共接口控制器
 */
@Slf4j
@Tag(name = "公共接口")
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final HotTagMapper hotTagMapper;
    private final SystemNotificationMapper systemNotificationMapper;

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("系统运行正常");
    }

    /**
     * 获取系统配置
     */
    @Operation(summary = "获取公开的系统配置")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("app_name", "校园易购");
        config.put("version", "1.0.0");
        config.put("upload_max_size", 5242880);
        config.put("upload_allowed_types", new String[]{"jpg", "jpeg", "png", "webp"});
        config.put("product_max_images", 9);
        return Result.success(config);
    }

    /**
     * 获取热门搜索标签
     */
    @Operation(summary = "获取热门搜索标签")
    @GetMapping("/hot-tags")
    public Result<List<HotTagVO>> getHotTags(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门搜索标签, limit={}", limit);

        List<HotTag> hotTags = hotTagMapper.selectList(
            new LambdaQueryWrapper<HotTag>()
                .eq(HotTag::getStatus, 1)
                .eq(HotTag::getDeleted, 0)
                .orderByAsc(HotTag::getSortOrder)
                .orderByDesc(HotTag::getSearchCount)
                .last("LIMIT " + limit)
        );

        List<HotTagVO> voList = hotTags.stream().map(tag -> {
            HotTagVO vo = new HotTagVO();
            vo.setTagId(tag.getTagId());
            vo.setKeyword(tag.getKeyword());
            vo.setSearchCount(tag.getSearchCount());
            vo.setSortOrder(tag.getSortOrder());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 获取首页公告
     */
    @Operation(summary = "获取首页公告")
    @GetMapping("/home-notice")
    public Result<Map<String, Object>> getHomeNotice() {
        log.info("获取首页公告");

        LocalDateTime now = LocalDateTime.now();

        List<SystemNotification> notifications = systemNotificationMapper.selectList(
            new LambdaQueryWrapper<SystemNotification>()
                .eq(SystemNotification::getStatus, 1) // 已发布
                .eq(SystemNotification::getDeleted, 0)
                .le(SystemNotification::getPublishTime, now) // 发布时间 <= 当前时间
                .or(wrapper -> wrapper
                    .isNull(SystemNotification::getEndTime)
                    .or()
                    .ge(SystemNotification::getEndTime, now) // 结束时间 >= 当前时间或为空
                )
                .orderByDesc(SystemNotification::getPriority)
                .orderByDesc(SystemNotification::getPublishTime)
                .last("LIMIT 1")
        );

        Map<String, Object> result = new HashMap<>();
        if (!notifications.isEmpty()) {
            SystemNotification notification = notifications.get(0);
            SystemNotificationVO vo = convertToVO(notification);
            result.put("notice", vo);
        } else {
            result.put("notice", null);
        }

        return Result.success(result);
    }

    /**
     * 转换为通知VO
     */
    private SystemNotificationVO convertToVO(SystemNotification notification) {
        SystemNotificationVO vo = new SystemNotificationVO();
        vo.setNotificationId(notification.getNotificationId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType());
        vo.setTypeDesc(getTypeDesc(notification.getType()));
        vo.setLinkType(notification.getLinkType());
        vo.setLinkUrl(notification.getLinkUrl());
        vo.setLinkProductId(notification.getLinkProductId());
        vo.setLinkOrderId(notification.getLinkOrderId());
        vo.setPublishTime(notification.getPublishTime());
        vo.setPriority(notification.getPriority());
        return vo;
    }

    /**
     * 获取通知类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) return "系统公告";
        switch (type) {
            case 1: return "系统公告";
            case 2: return "活动通知";
            case 3: return "账户提醒";
            case 4: return "交易提醒";
            default: return "系统公告";
        }
    }

    /**
     * 获取可用主题列表
     */
    @Operation(summary = "获取可用主题列表")
    @GetMapping("/themes")
    public Result<List<Map<String, Object>>> getAvailableThemes() {
        log.info("获取可用主题列表");

        List<Map<String, Object>> themes = new ArrayList<>();

        // 浅色主题
        Map<String, Object> lightTheme = new HashMap<>();
        lightTheme.put("value", "light");
        lightTheme.put("label", "浅色");
        lightTheme.put("icon", "sunny");
        lightTheme.put("default", true);
        themes.add(lightTheme);

        // 深色主题
        Map<String, Object> darkTheme = new HashMap<>();
        darkTheme.put("value", "dark");
        darkTheme.put("label", "深色");
        darkTheme.put("icon", "moon");
        darkTheme.put("default", false);
        themes.add(darkTheme);

        // 跟随系统
        Map<String, Object> autoTheme = new HashMap<>();
        autoTheme.put("value", "auto");
        autoTheme.put("label", "跟随系统");
        autoTheme.put("icon", "desktop");
        autoTheme.put("default", false);
        themes.add(autoTheme);

        return Result.success(themes);
    }
}
