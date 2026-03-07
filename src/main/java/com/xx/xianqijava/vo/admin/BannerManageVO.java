package com.xx.xianqijava.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图管理VO - 管理端
 */
@Data
@Schema(description = "轮播图管理VO")
public class BannerManageVO {

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

    @Schema(description = "链接类型描述")
    public String getLinkTypeDesc() {
        switch (linkType) {
            case 1:
                return "无";
            case 2:
                return "外链";
            case 3:
                return "商品详情";
            case 4:
                return "功能页面";
            default:
                return "未知";
        }
    }

    @Schema(description = "跳转URL")
    private String linkUrl;

    @Schema(description = "关联商品ID")
    private Long linkProductId;

    @Schema(description = "关联商品标题")
    private String linkProductTitle;

    @Schema(description = "功能页面路径")
    private String linkPagePath;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态描述")
    public String getStatusDesc() {
        return status == 1 ? "启用" : "禁用";
    }

    @Schema(description = "开始展示时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束展示时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "点击次数")
    private Integer clickCount;

    @Schema(description = "曝光次数")
    private Integer exposureCount;

    @Schema(description = "点击率（%）")
    public Double getClickRate() {
        if (exposureCount == null || exposureCount == 0) {
            return 0.0;
        }
        return (double) clickCount / exposureCount * 100;
    }

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "是否正在展示")
    public Boolean getIsShowing() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null || status != 1) {
            return false;
        }
        boolean afterStart = startTime == null || now.isAfter(startTime) || now.isEqual(startTime);
        boolean beforeEnd = endTime == null || now.isBefore(endTime) || now.isEqual(endTime);
        return afterStart && beforeEnd;
    }
}
