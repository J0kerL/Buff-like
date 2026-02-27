package com.buff.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户库存实体类
 *
 * @author Administrator
 */
@Data
public class UserInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 归属用户ID
     */
    private Long userId;

    /**
     * 关联的模板ID
     */
    private Long templateId;

    /**
     * 磨损度 (0.00-1.00)
     */
    private BigDecimal wearValue;

    /**
     * 图案模板编号
     */
    private Integer patternIndex;

    /**
     * 状态: 0=在库, 1=出售中, 2=交易锁定
     */
    private Integer status;

    /**
     * 获得时间
     */
    private LocalDateTime getTime;

    /**
     * 是否已删除：0=正常，1=已删除（软删除）
     */
    private Integer isDeleted;
}
