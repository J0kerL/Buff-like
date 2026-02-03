package com.buff.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单实体类
 *
 * @author Administrator
 */
@Data
public class TradeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单号(唯一)
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 关联的市场挂单ID
     */
    private Long listingId;

    /**
     * 关联的库存物品ID
     */
    private Long inventoryId;

    /**
     * 成交金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态: 0=待支付, 1=待发货, 2=已发货, 3=交易成功, 4=已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime deliverTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}
