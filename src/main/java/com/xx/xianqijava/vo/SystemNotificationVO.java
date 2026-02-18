package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知视图对象
 */
@Data
@Schema(description = "系统通知")
public class SystemNotificationVO {

    @Schema(description = "通知ID")
    private Long notificationId;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "通知类型：1-系统公告，2-活动通知，3-账户提醒，4-交易提醒")
    private Integer type;

    @Schema(description = "类型描述")
    private String typeDesc;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "跳转链接类型：1-无，2-网页，3-商品详情，4-订单详情")
    private Integer linkType;

    @Schema(description = "跳转URL")
    private String linkUrl;

    @Schema(description = "关联商品ID")
    private Long linkProductId;

    @Schema(description = "关联订单ID")
    private Long linkOrderId;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "优先级：1-低，2-中，3-高")
    private Integer priority;
}
