package com.xx.xianqijava.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价详情 VO
 */
@Data
@Schema(description = "评价详情")
public class EvaluationVO {

    @Schema(description = "评价ID")
    private Long evaluationId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "评分：1-5星")
    private Integer rating;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价标签")
    private String tags;

    @Schema(description = "评价人ID")
    private Long evaluatorId;

    @Schema(description = "评价人昵称")
    private String evaluatorNickname;

    @Schema(description = "评价人头像")
    private String evaluatorAvatar;

    @Schema(description = "被评价人ID")
    private Long evaluatedUserId;

    @Schema(description = "被评价人昵称")
    private String evaluatedUserNickname;

    @Schema(description = "被评价人头像")
    private String evaluatedUserAvatar;

    @Schema(description = "评价对象类型：1-评价卖家 2-评价买家")
    private Integer targetType;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品图片")
    private String productImage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
