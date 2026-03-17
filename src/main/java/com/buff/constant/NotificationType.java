package com.buff.constant;

/**
 * 通知类型常量
 *
 * @author Administrator
 */
public interface NotificationType {

    /**
     * 买家付款 - 通知卖家
     */
    int BUYER_PAID = 1;

    /**
     * 卖家发货 - 通知买家
     */
    int SELLER_DELIVERED = 2;

    /**
     * 交易成功 - 通知卖家
     */
    int TRADE_SUCCESS = 3;

    /**
     * 订单取消 - 通知对方
     */
    int ORDER_CANCELLED = 4;

    /**
     * 卖家拒绝发货 - 通知买家（已退款）
     */
    int SELLER_REJECTED = 5;
}
