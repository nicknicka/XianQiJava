package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 转赠记录VO
 */
@Data
@Schema(description = "转赠记录")
public class TransferRecordVO {

    @Schema(description = "转赠记录ID")
    private Long transferId;

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "共享物品标题")
    private String shareItemTitle;

    @Schema(description = "共享物品图片")
    private String shareItemImage;

    @Schema(description = "转出人ID")
    private Long fromUserId;

    @Schema(description = "转出人昵称")
    private String fromUserNickname;

    @Schema(description = "转出人头像")
    private String fromUserAvatar;

    @Schema(description = "接收人ID")
    private Long toUserId;

    @Schema(description = "接收人昵称")
    private String toUserNickname;

    @Schema(description = "接收人头像")
    private String toUserAvatar;

    @Schema(description = "转赠说明")
    private String transferNote;

    @Schema(description = "接受状态：0-待确认，1-已接受，2-已拒绝")
    private Integer acceptStatus;

    @Schema(description = "接受状态描述")
    private String acceptStatusDesc;

    @Schema(description = "确认时间")
    private String confirmTime;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "转赠完成时间")
    private String completeTime;

    @Schema(description = "创建时间")
    private String createTime;
}
