package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志VO
 */
@Data
@Schema(description = "操作日志")
public class OperationLogVO {

    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "执行时长（毫秒）")
    private Long executeTime;

    @Schema(description = "执行状态：1-成功，0-失败")
    private Integer status;

    @Schema(description = "执行状态描述")
    private String statusDesc;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "操作时间")
    private String createTime;
}
