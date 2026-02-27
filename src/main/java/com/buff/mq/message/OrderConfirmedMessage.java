package com.buff.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单确认收货消息
 * <p>
 * 买家确认收货后，主事务完成订单状态更新并发送此消息；
 * 消费者异步完成：卖家余额打款、资金流水记录、库存所有权转移、挂单标记已售出。
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 买家ID（库存新归属人） */
    private Long buyerId;

    /** 卖家ID（收款方） */
    private Long sellerId;

    /** 交易金额 */
    private BigDecimal amount;

    /** 关联库存ID */
    private Long inventoryId;

    /** 关联挂单ID */
    private Long listingId;
}
