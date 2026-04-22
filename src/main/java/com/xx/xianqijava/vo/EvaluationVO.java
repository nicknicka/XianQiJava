package com.xx.xianqijava.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价详情 VO
 */
@Data
@Schema(description = "评价详情")
public class EvaluationVO {

    @Schema(description = "评价ID，兼容字段")
    private String id;

    @Schema(description = "评价ID")
    private String evaluationId;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "评分：1-5星")
    private Integer rating;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价标签")
    private List<String> tags;

    @Schema(description = "评价图片")
    private List<String> images;

    @Schema(description = "追评内容")
    private String appendContent;

    @Schema(description = "追评图片")
    private List<String> appendImages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "追评时间")
    private LocalDateTime appendTime;

    @Schema(description = "是否已追评")
    private Boolean hasAppend;

    @Schema(description = "评价人ID")
    private String evaluatorId;

    @Schema(description = "评价人ID，兼容字段")
    private String fromUserId;

    @Schema(description = "评价人昵称")
    private String evaluatorNickname;

    @Schema(description = "评价人昵称，兼容字段")
    private String evaluatorName;

    @Schema(description = "评价人昵称，兼容字段")
    private String fromUserName;

    @Schema(description = "评价人头像")
    private String evaluatorAvatar;

    @Schema(description = "评价人头像，兼容字段")
    private String fromUserAvatar;

    @Schema(description = "被评价人ID")
    private String evaluatedUserId;

    @Schema(description = "被评价人ID，兼容字段")
    private String toUserId;

    @Schema(description = "被评价人昵称")
    private String evaluatedUserNickname;

    @Schema(description = "被评价人昵称，兼容字段")
    private String toUserName;

    @Schema(description = "被评价人头像")
    private String evaluatedUserAvatar;

    @Schema(description = "被评价人头像，兼容字段")
    private String toUserAvatar;

    @Schema(description = "评价对象类型：1-评价卖家 2-评价买家")
    private Integer targetType;

    @Schema(description = "商品ID")
    private String productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品标题，兼容字段")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "商品价格（订单成交价）")
    private java.math.BigDecimal productPrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间，兼容字段")
    private LocalDateTime createdAt;
}
