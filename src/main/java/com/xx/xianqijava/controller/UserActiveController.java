package com.xx.xianqijava.controller;

import com.xx.xianqijava.service.UserActiveService;
import com.xx.xianqijava.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户活跃时间接口
 */
@Slf4j
@Tag(name = "用户活跃时间")
@RestController
@RequestMapping("/api/user-active")
public class UserActiveController {

    @Autowired
    private UserActiveService userActiveService;

    /**
     * 更新用户最后活跃时间
     * 通常在用户登录、进行重要操作时自动调用
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "更新用户最后活跃时间")
    @PostMapping("/update/{userId}")
    public Result<Void> updateLastActiveTime(@PathVariable String userId) {
        try {
            userActiveService.updateLastActiveTime(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("更新用户活跃时间失败", e);
            return Result.error("更新失败");
        }
    }
}
