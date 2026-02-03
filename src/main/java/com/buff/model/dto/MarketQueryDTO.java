package com.buff.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 市场查询请求DTO
 * @author Administrator

 */
@Data
@Schema(description = "市场查询请求")
public class MarketQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键字搜索", example = "AK-47")
    private String keyword;

    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    @Schema(description = "最小价格", example = "100.00")
    private BigDecimal minPrice;

    @Schema(description = "最大价格", example = "200.00")
    private BigDecimal maxPrice;

    @Schema(description = "最小磨损度", example = "0.15")
    private BigDecimal minWear;

    @Schema(description = "最大磨损度", example = "0.30")
    private BigDecimal maxWear;

    @Schema(description = "排序字段 (price/createTime)", example = "price")
    private String sortField = "price";

    @Schema(description = "排序方式 (asc/desc)", example = "asc")
    private String sortOrder = "asc";

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
