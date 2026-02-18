package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 共享物品图片表
 */
@Data
@TableName("share_item_image")
@Schema(description = "共享物品图片")
public class ShareItemImage {

    @TableId(type = IdType.AUTO)
    @Schema(description = "图片ID")
    private Long imageId;

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String imageThumbnailUrl;

    @Schema(description = "中等尺寸图片URL")
    private String imageMediumUrl;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "图片宽度")
    private Integer width;

    @Schema(description = "图片高度")
    private Integer height;

    @Schema(description = "是否为封面：0-否，1-是")
    private Integer isCover;

    @Schema(description = "状态：0-正常，1-已删除")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
