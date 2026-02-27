package com.buff.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水VO
 *
 * @author Administrator
 */
@Data
@Schema(description = "资金流水信息")
public class WalletLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "类型: 1=充值, 2=提现, 3=购买支出, 4=出售收入")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "变动金额")
    private BigDecimal amount;

    @Schema(description = "变动后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
