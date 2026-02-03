package com.buff.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场商品VO
 *
 * @author Administrator
 */
@Data
@Schema(description = "市场商品信息")
public class MarketListingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "挂单ID")
    private Long id;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "卖家用户名")
    private String sellerName;

    @Schema(description = "库存ID")
    private Long inventoryId;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "饰品名称")
    private String itemName;

    @Schema(description = "图片URL")
    private String iconUrl;

    @Schema(description = "磨损度")
    private BigDecimal wearValue;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "上架时间")
    private LocalDateTime createTime;
}
