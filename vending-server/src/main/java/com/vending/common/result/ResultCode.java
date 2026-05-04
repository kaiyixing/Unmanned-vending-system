package com.vending.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_WRONG(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),

    CABINET_NOT_FOUND(2001, "货柜不存在"),
    CABINET_NOT_AVAILABLE(2002, "货柜不可用"),

    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_OFF_SHELF(3002, "商品已下架"),

    INSUFFICIENT_STOCK(4001, "库存不足"),
    INVENTORY_ERROR(4002, "库存操作失败"),

    ORDER_NOT_FOUND(5001, "订单不存在"),
    ORDER_STATUS_ERROR(5002, "订单状态异常"),
    ORDER_EXPIRED(5003, "订单已过期"),

    PAYMENT_FAILED(6001, "支付失败"),
    PAYMENT_TIMEOUT(6002, "支付超时"),

    PICKUP_CODE_EXPIRED(7001, "取货码已过期"),
    PICKUP_CODE_USED(7002, "取货码已使用"),

    TOKEN_INVALID(8001, "Token无效或已过期");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
