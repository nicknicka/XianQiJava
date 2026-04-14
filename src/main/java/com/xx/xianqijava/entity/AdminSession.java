package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员在线会话实体
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("admin_session")
public class AdminSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 管理员ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adminId;

    /**
     * JWT Token
     */
    private String token;

    /**
     * IP地址
     */
    private String ip;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 登录时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime loginTime;

    /**
     * 最后活跃时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastActiveTime;

    /**
     * 状态：0-已退出 1-在线
     */
    private Integer status;
}
