package com.buff.constant;

/**
 * 挂单状态常量
 *
 * @author Administrator
 */
public interface ListingStatus {

    /**
     * 上架中
     */
    int ON_SALE = 0;

    /**
     * 已被购买(生成订单)
     */
    int PURCHASED = 1;

    /**
     * 已下架
     */
    int OFF_SALE = 2;

    /**
     * 已售出
     */
    int SOLD = 3;
}
