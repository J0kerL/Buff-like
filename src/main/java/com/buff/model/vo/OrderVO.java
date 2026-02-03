package com.buff.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单VO
 *
 * @author Administrator
 */
@Data
@Schema(description = "订单信息")
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "饰品名称")
    private String itemName;

    @Schema(description = "图片URL")
    private String iconUrl;

    @Schema(description = "成交金额")
    private BigDecimal totalAmount;

    @Schema(description = "状态: 0=待支付, 1=待发货, 2=已发货, 3=交易成功, 4=已取消")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "发货时间")
    private LocalDateTime deliverTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;
}
