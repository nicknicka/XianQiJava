package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快捷回复视图对象
 */
@Data
@Schema(description = "快捷回复模板")
public class QuickReplyVO {

    @Schema(description = "回复ID")
    private Long replyId;

    @Schema(description = "用户ID（0表示系统预设）")
    private Long userId;

    @Schema(description = "模板标题")
    private String title;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否系统预设：0-否，1-是")
    private Integer isSystem;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
