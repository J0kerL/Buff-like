package com.buff.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 饰品模板VO
 * @author Administrator
 */

@Data
@Schema(description = "饰品模板信息")
public class ItemTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "饰品名称")
    private String name;

    @Schema(description = "图片URL")
    private String iconUrl;

    @Schema(description = "稀有度")
    private String rarity;

    @Schema(description = "类型")
    private String type;
}
