package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈视图对象
 */
@Data
@Schema(description = "用户反馈")
public class UserFeedbackVO {

    @Schema(description = "反馈ID")
    private Long feedbackId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "反馈类型：1-功能建议，2-Bug反馈，3-投诉，4-其他")
    private Integer type;

    @Schema(description = "类型描述")
    private String typeDesc;

    @Schema(description = "反馈标题")
    private String title;

    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "图片列表")
    private String images;

    @Schema(description = "处理状态：0-待处理，1-处理中，2-已处理，3-已驳回")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "处理备注")
    private String handleNote;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
}
