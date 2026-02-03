package com.buff.constant;

/**
 * 订单状态常量
 *
 * @author Administrator
 */
public interface OrderStatus {

    /**
     * 待支付
     */
    int PENDING_PAY = 0;

    /**
     * 待发货
     */
    int PAID_WAIT_DELIVERY = 1;

    /**
     * 已发货
     */
    int DELIVERED = 2;

    /**
     * 交易成功
     */
    int SUCCESS = 3;

    /**
     * 已取消
     */
    int CANCELLED = 4;
}
