package com.buff.constant;

/**
 * 库存状态常量
 *
 * @author Administrator
 */
public interface InventoryStatus {

    /**
     * 在库
     */
    int IN_STOCK = 0;

    /**
     * 出售中
     */
    int ON_SALE = 1;

    /**
     * 交易锁定
     */
    int LOCKED = 2;
}
