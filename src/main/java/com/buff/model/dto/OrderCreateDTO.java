package com.buff.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建订单请求DTO
 *
 * @author Administrator
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "挂单ID", example = "1")
    @NotNull(message = "挂单ID不能为空")
    private Long listingId;
}
