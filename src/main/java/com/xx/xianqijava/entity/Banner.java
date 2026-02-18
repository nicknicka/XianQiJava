package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 轮播图表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("banner")
@Schema(description = "轮播图")
public class Banner extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "轮播图ID")
    private Long bannerId;

    @Schema(description = "轮播图标题")
    private String title;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String imageThumbnailUrl;

    @Schema(description = "链接类型：1-无，2-外链，3-商品详情，4-功能页面")
    private Integer linkType;

    @Schema(description = "跳转URL（外链时使用）")
    private String linkUrl;

    @Schema(description = "关联商品ID（link_type=3时使用）")
    private Long linkProductId;

    @Schema(description = "功能页面路径（link_type=4时使用）")
    private String linkPagePath;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "开始展示时间")
    private java.time.LocalDateTime startTime;

    @Schema(description = "结束展示时间")
    private java.time.LocalDateTime endTime;

    @Schema(description = "点击次数")
    private Integer clickCount;

    @Schema(description = "曝光次数")
    private Integer exposureCount;
}
