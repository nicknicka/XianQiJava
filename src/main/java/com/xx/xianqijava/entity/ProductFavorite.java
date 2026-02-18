package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品收藏表
 */
@Data
@TableName("product_favorite")
@Schema(description = "商品收藏")
public class ProductFavorite {

    @TableId(type = IdType.AUTO)
    @Schema(description = "收藏ID")
    private Long favoriteId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}
