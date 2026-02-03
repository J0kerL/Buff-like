package com.buff.constant;

/**
 * 资金流水类型常量
 *
 * @author Administrator
 */
public interface WalletLogType {

    /**
     * 充值
     */
    int RECHARGE = 1;

    /**
     * 提现
     */
    int WITHDRAW = 2;

    /**
     * 购买支出
     */
    int PURCHASE = 3;

    /**
     * 出售收入
     */
    int SALE_INCOME = 4;
}
