package com.buff.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 饰品模板实体类
 * @author Administrator
 */
@Data

public class ItemTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 饰品名称 (如: AK-47 | 二西莫夫)
     */
    private String name;

    /**
     * 饰品图片URL
     */
    private String iconUrl;

    /**
     * 稀有度 (普通/受限/保密/隐秘)
     */
    private String rarity;

    /**
     * 类型 (步枪/手套/匕首)
     */
    private String type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
