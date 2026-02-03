package com.buff.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存物品VO
 *
 * @author Administrator
 */
@Data
@Schema(description = "库存物品信息")
public class InventoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "库存ID")
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "饰品名称")
    private String itemName;

    @Schema(description = "图片URL")
    private String iconUrl;

    @Schema(description = "磨损度")
    private BigDecimal wearValue;

    @Schema(description = "图案编号")
    private Integer patternIndex;

    @Schema(description = "状态: 0=在库, 1=出售中, 2=交易锁定")
    private Integer status;

    @Schema(description = "获得时间")
    private LocalDateTime getTime;
}
