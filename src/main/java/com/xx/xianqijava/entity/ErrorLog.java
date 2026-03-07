package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统异常日志实体
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("error_log")
public class ErrorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 异常ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 异常级别：ERROR/WARN/INFO
     */
    private String level;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 异常类型
     */
    private String exceptionType;

    /**
     * 堆栈跟踪
     */
    private String stackTrace;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 用户类型：1-普通用户 2-管理员
     */
    private Integer userType;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 发生时间
     */
    private LocalDateTime occurTime;
}
