package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 转赠记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("transfer_record")
@Schema(description = "转赠记录")
public class TransferRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "转赠记录ID")
    private Long transferId;

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "转出人ID")
    private Long fromUserId;

    @Schema(description = "接收人ID")
    private Long toUserId;

    @Schema(description = "转赠说明")
    private String transferNote;

    @Schema(description = "接收人确认状态：0-待确认，1-已接受，2-已拒绝")
    private Integer acceptStatus;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "转赠完成时间")
    private LocalDateTime completeTime;
}
