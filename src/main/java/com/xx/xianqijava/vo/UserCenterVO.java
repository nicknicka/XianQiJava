package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户中心视图对象
 */
@Data
@Schema(description = "用户中心信息")
public class UserCenterVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "信用分数")
    private Integer creditScore;

    @Schema(description = "我的发布数量")
    private Integer productCount;

    @Schema(description = "我的订单数量")
    private Integer orderCount;

    @Schema(description = "我的收藏数量")
    private Integer favoriteCount;

    @Schema(description = "收到的评价数量")
    private Integer evaluationCount;

    @Schema(description = "最近发布的商品")
    private List<ProductVO> recentProducts;
}
