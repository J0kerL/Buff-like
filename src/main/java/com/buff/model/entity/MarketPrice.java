package com.buff.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市场价格实体类
 *
 * @author Administrator
 */
@Data
public class MarketPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 饰品模板ID
     */
    private Long templateId;

    /**
     * 市场参考价格
     */
    private BigDecimal price;

    /**
     * 价格来源（BUFF/Steam等）
     */
    private String source;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
