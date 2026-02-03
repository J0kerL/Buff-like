package com.buff.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场在售商品实体类
 * @author Administrator

 */
@Data
public class MarketListing implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 关联的库存ID
     */
    private Long inventoryId;

    /**
     * 冗余模板ID，方便查询
     */
    private Long templateId;

    /**
     * 出售价格
     */
    private BigDecimal price;

    /**
     * 状态: 0=上架中, 1=已被购买(生成订单), 2=已下架, 3=已售出
     */
    private Integer status;

    /**
     * 乐观锁(防止并发购买)
     */
    private Integer version;

    /**
     * 上架时间
     */
    private LocalDateTime createTime;
}
