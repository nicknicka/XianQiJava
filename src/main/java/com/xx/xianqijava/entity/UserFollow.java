package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注表
 */
@Data
@TableName("user_follow")
@Schema(description = "用户关注")
public class UserFollow {

    @TableId(type = IdType.AUTO)
    @Schema(description = "关注ID")
    private Long followId;

    @Schema(description = "关注者ID（主动关注的人）")
    private Long followerId;

    @Schema(description = "被关注者ID（被关注的人）")
    private Long followingId;

    @Schema(description = "关注时间")
    private LocalDateTime createTime;
}
