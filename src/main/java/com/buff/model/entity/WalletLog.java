package com.buff.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水实体类
 *
 * @author Administrator
 */
@Data
public class WalletLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型: 1=充值, 2=提现, 3=购买支出, 4=出售收入
     */
    private Integer type;

    /**
     * 变动金额(+/-)
     */
    private BigDecimal amount;

    /**
     * 变动后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
