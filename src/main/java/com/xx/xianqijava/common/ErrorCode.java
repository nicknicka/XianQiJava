package com.xx.xianqijava.common;

import lombok.Getter;

/**
 * 业务错误码枚举
 */
@Getter
public enum ErrorCode {

    // 通用错误码 1xxxx
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token无效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    REQUEST_TIMEOUT(408, "请求超时"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关错误码 10xxx
    USER_NOT_FOUND(10001, "用户不存在"),
    USERNAME_EXISTS(10002, "用户名已存在"),
    PHONE_EXISTS(10003, "手机号已注册"),
    STUDENT_ID_EXISTS(10004, "学号已注册"),
    PASSWORD_ERROR(10005, "密码错误"),
    USER_BANNED(10006, "用户已被封禁"),
    USER_DISABLED(10007, "用户已被禁用"),
    OLD_PASSWORD_ERROR(10008, "原密码错误"),
    VERIFICATION_CODE_ERROR(10009, "验证码错误"),
    VERIFICATION_CODE_EXPIRED(10010, "验证码已过期"),

    // 商品相关错误码 20xxx
    PRODUCT_NOT_FOUND(20001, "商品不存在"),
    PRODUCT_OFFLINE(20002, "商品已下架"),
    PRODUCT_SOLD_OUT(20003, "商品已售出"),
    PRODUCT_TITLE_TOO_LONG(20004, "商品标题过长"),
    PRODUCT_DESC_TOO_LONG(20005, "商品描述过长"),
    PRODUCT_IMAGE_EXCEED_LIMIT(20006, "商品图片数量超过限制"),
    PRODUCT_IMAGE_NOT_FOUND(20007, "商品图片不存在"),
    CATEGORY_NOT_FOUND(20008, "分类不存在"),

    // 共享物品相关错误码 21xxx
    SHARE_ITEM_NOT_FOUND(21001, "共享物品不存在"),
    SHARE_ITEM_NOT_AVAILABLE(21002, "共享物品不可借用"),
    SHARE_ITEM_BORROWED(21003, "共享物品借用中"),
    SHARE_DURATION_EXCEED_LIMIT(21004, "借用时长超过限制"),

    // 订单相关错误码 30xxx
    ORDER_NOT_FOUND(30001, "订单不存在"),
    ORDER_STATUS_ERROR(30002, "订单状态错误"),
    ORDER_TIMEOUT(30003, "订单已超时"),
    ORDER_PAID(30004, "订单已支付"),
    ORDER_CANCELLED(30005, "订单已取消"),
    ORDER_COMPLETED(30006, "订单已完成"),
    CANNOT_BUY_SELF_PRODUCT(30007, "不能购买自己的商品"),
    ORDER_AMOUNT_ERROR(30008, "订单金额错误"),

    // 评价相关错误码 31xxx
    EVALUATION_EXISTS(31001, "已评价过该订单"),
    EVALUATION_NOT_FOUND(31002, "评价不存在"),
    SCORE_OUT_OF_RANGE(31003, "评分超出范围"),

    // 聊天相关错误码 40xxx
    CONVERSATION_NOT_FOUND(40001, "会话不存在"),
    MESSAGE_NOT_FOUND(40002, "消息不存在"),
    MESSAGE_RECALLED(40003, "消息已撤回"),
    MESSAGE_RECALL_TIMEOUT(40004, "消息撤回超时"),
    CANNOT_SEND_TO_BLOCKED(40005, "无法发送消息给已拉黑的用户"),
    USER_BLOCKED(40006, "用户已在黑名单中"),

    // 收藏相关错误码 50xxx
    FAVORITE_EXISTS(50001, "已收藏该商品"),
    FAVORITE_NOT_FOUND(50002, "收藏不存在"),

    // 文件上传相关错误码 60xxx
    FILE_UPLOAD_ERROR(60001, "文件上传失败"),
    FILE_SIZE_EXCEED(60002, "文件大小超过限制"),
    FILE_TYPE_ERROR(60003, "文件类型错误"),
    FILE_NOT_FOUND(60004, "文件不存在"),

    // 敏感词相关错误码 70xxx
    SENSITIVE_WORD_DETECTED(70001, "内容包含敏感词"),
    CONTENT_AUDIT_FAILED(70002, "内容审核不通过"),

    // OSS 相关错误码 80xxx
    OSS_UPLOAD_ERROR(80001, "OSS上传失败"),
    OSS_CONFIG_ERROR(80002, "OSS配置错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
