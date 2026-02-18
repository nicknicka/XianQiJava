package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户反馈表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_feedback")
@Schema(description = "用户反馈")
public class UserFeedback extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "反馈ID")
    private Long feedbackId;

    @Schema(description = "用户ID（可为空，匿名反馈）")
    private Long userId;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "反馈类型：1-功能建议，2-Bug反馈，3-投诉，4-其他")
    private Integer type;

    @Schema(description = "反馈标题")
    private String title;

    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "图片列表（JSON数组）")
    private String images;

    @Schema(description = "处理状态：0-待处理，1-处理中，2-已处理，3-已驳回")
    private Integer status;

    @Schema(description = "处理人ID（管理员）")
    private Long handlerId;

    @Schema(description = "处理备注")
    private String handleNote;

    @Schema(description = "处理时间")
    private java.time.LocalDateTime handleTime;
}
