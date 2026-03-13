package com.buff.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 库存查询DTO
 *
 * @author Administrator
 */
@Data
@Schema(description = "库存查询请求")
public class InventoryQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "库存状态: 0=在库, 1=出售中, 2=交易锁定")
    private Integer status;

    @Schema(description = "饰品类型")
    private String type;

    @Schema(description = "多类型/武器筛选（逗号分隔的选择值，如 'TYPE:步枪,SUB:AK-47:步枪,TYPE:匕首'）")
    private String typeSelects;

    @Schema(description = "外观筛选（逗号分隔，如 '崭新出厂,战痕累累'")
    private String wears;

    @Schema(description = "关键词搜索（饰品名称）")
    private String keyword;
}
