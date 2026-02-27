package com.buff.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 库存统计VO
 *
 * @author Administrator
 */
@Data
@Schema(description = "库存统计信息")
public class InventoryStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总饰品数量")
    private Long totalCount;

    @Schema(description = "在库数量")
    private Long inStockCount;

    @Schema(description = "出售中数量")
    private Long onSaleCount;

    @Schema(description = "交易锁定数量")
    private Long lockedCount;

    @Schema(description = "库存总价值（预估）")
    private BigDecimal totalValue;

    @Schema(description = "按稀有度分组统计 (稀有度 -> 数量)")
    private Map<String, Long> rarityStats;

    @Schema(description = "按类型分组统计 (类型 -> 数量)")
    private Map<String, Long> typeStats;
}
