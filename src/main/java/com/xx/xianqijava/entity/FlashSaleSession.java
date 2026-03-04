package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀场次表实体（已简化，合并了活动功能）
 */
@Data
@TableName("flash_sale_session")
@Schema(description = "秒杀场次")
public class FlashSaleSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "场次ID")
    private Long sessionId;

    @Schema(description = "场次名称（原活动名称）")
    private String name;

    @Schema(description = "场次描述")
    private String description;

    @Schema(description = "场次时间点（HH:mm格式，如 10:00）")
    private String sessionTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime startTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime endTime;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "场次类型：1-默认固定场次，2-特殊临时场次")
    private Integer sessionType;

    @Schema(description = "是否启用：0-禁用（暂停/取消），1-启用")
    private Integer enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
