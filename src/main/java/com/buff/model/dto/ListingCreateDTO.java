package com.buff.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 上架商品请求DTO
 * @author Administrator

 */
@Data
@Schema(description = "上架商品请求")
public class ListingCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "库存ID", example = "1")
    @NotNull(message = "库存ID不能为空")
    private Long inventoryId;

    @Schema(description = "出售价格", example = "150.50")
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
}
