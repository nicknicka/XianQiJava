package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
@TableName("operation_log")
@Schema(description = "操作日志")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "操作用户ID（0表示系统操作）")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "操作模块：user/product/order/share_item/system等")
    private String module;

    @Schema(description = "操作类型：login/create/update/delete/query/export等")
    private String action;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "请求方法：GET/POST/PUT/DELETE")
    private String requestMethod;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求参数（JSON）")
    private String requestParams;

    @Schema(description = "响应结果（JSON）")
    private String responseResult;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "执行时长（毫秒）")
    private Long executeTime;

    @Schema(description = "执行状态：1-成功，0-失败")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
