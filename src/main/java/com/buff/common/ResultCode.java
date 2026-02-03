package com.buff.common;

import lombok.Getter;

/**
 * 统一状态码枚举
 * @author Administrator
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 参数相关 4xx
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(401, "缺少必要参数"),
    PARAM_TYPE_ERROR(402, "参数类型错误"),

    // 认证授权相关 401-403
    UNAUTHORIZED(401, "未授权，请先登录"),
    TOKEN_EXPIRED(401, "登录已过期，请重新登录"),
    TOKEN_INVALID(401, "无效的令牌"),
    FORBIDDEN(403, "没有权限访问"),

    // 业务相关 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    BALANCE_NOT_ENOUGH(1004, "余额不足"),
    
    ITEM_NOT_FOUND(2001, "饰品不存在"),
    ITEM_NOT_IN_INVENTORY(2002, "饰品不在库存中"),
    ITEM_ALREADY_ON_SALE(2003, "饰品已上架"),
    ITEM_NOT_ON_SALE(2004, "饰品未上架"),
    
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_TIMEOUT(3003, "订单已超时"),
    
    LISTING_NOT_FOUND(4001, "商品不存在"),
    LISTING_SOLD_OUT(4002, "商品已售出"),
    LISTING_PRICE_ERROR(4003, "价格设置错误"),

    // 系统相关 5xxx
    SYSTEM_ERROR(5000, "系统错误"),
    DATABASE_ERROR(5001, "数据库错误"),
    REDIS_ERROR(5002, "缓存错误"),
    FILE_UPLOAD_ERROR(5003, "文件上传失败"),
    CONCURRENT_ERROR(5004, "并发操作失败，请重试");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
