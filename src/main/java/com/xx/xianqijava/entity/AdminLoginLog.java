package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员登录日志实体
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("admin_login_log")
public class AdminLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * IP地址
     */
    private String ip;

    /**
     * IP归属地
     */
    private String location;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态：0-失败 1-成功
     */
    private Integer status;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
