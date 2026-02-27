package com.buff.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提现请求DTO
 *
 * @author Administrator
 */
@Data
@Schema(description = "提现请求")
public class WithdrawDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提现金额", example = "50.00")
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额必须大于0")
    private BigDecimal amount;
}
