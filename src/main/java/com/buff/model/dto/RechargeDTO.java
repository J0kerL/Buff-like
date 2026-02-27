package com.buff.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充值请求DTO
 *
 * @author Administrator
 */
@Data
@Schema(description = "充值请求")
public class RechargeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "充值金额", example = "100.00")
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;
}
