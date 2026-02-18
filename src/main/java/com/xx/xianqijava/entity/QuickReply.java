package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 快捷回复模板表
 */
@Data
@TableName("quick_reply")
@Schema(description = "快捷回复模板")
public class QuickReply {

    @TableId(type = IdType.AUTO)
    @Schema(description = "回复ID")
    private Long replyId;

    @Schema(description = "用户ID（0表示系统预设）")
    private Long userId;

    @Schema(description = "模板标题")
    private String title;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "分类：交易-询问/交易-确认/其他")
    private String category;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否系统预设：0-否，1-是")
    private Integer isSystem;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
