package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报视图对象
 */
@Data
@Schema(description = "举报记录")
public class ReportVO {

    @Schema(description = "举报ID")
    private Long reportId;

    @Schema(description = "举报人ID")
    private Long reporterId;

    @Schema(description = "举报人昵称")
    private String reporterNickname;

    @Schema(description = "举报人头像")
    private String reporterAvatar;

    @Schema(description = "被举报人ID")
    private Long reportedUserId;

    @Schema(description = "被举报人昵称")
    private String reportedUserNickname;

    @Schema(description = "被举报人头像")
    private String reportedUserAvatar;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "证据图片")
    private String evidenceImages;

    @Schema(description = "处理状态：0-待处理，1-已处理，2-已驳回")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "管理员备注")
    private String adminNote;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
}
