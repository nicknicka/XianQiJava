package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 支付密码检查结果 VO
 */
@Data
@Schema(description = "支付密码检查结果响应")
public class PayPasswordCheckVO {

    @Schema(description = "是否已设置支付密码：false-未设置，true-已设置")
    private Boolean hasPayPassword;

    public PayPasswordCheckVO() {
    }

    public PayPasswordCheckVO(Boolean hasPayPassword) {
        this.hasPayPassword = hasPayPassword != null ? hasPayPassword : false;
    }
}
