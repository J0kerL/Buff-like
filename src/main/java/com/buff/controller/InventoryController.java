package com.buff.controller;

import com.buff.common.PageResult;
import com.buff.common.Result;
import com.buff.model.dto.InventoryQueryDTO;
import com.buff.model.vo.InventoryStatisticsVO;
import com.buff.model.vo.InventoryVO;
import com.buff.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存控制器
 *
 * @author Administrator
 */
@Tag(name = "库存管理", description = "用户库存相关接口")
@Validated
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "查看我的库存", description = "分页查询当前用户的所有饰品库存，支持按状态、类型、稀有度筛选")
    @GetMapping("/my")
    public Result<PageResult<InventoryVO>> getMyInventory(@Valid InventoryQueryDTO queryDTO) {
        PageResult<InventoryVO> result = inventoryService.getMyInventory(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "查看库存详情", description = "查看单个库存饰品的详细信息")
    @GetMapping("/{id}")
    public Result<InventoryVO> getInventoryDetail(
            @Parameter(description = "库存ID", example = "1")
            @PathVariable Long id) {
        InventoryVO inventoryVO = inventoryService.getInventoryDetail(id);
        return Result.success(inventoryVO);
    }

    @Operation(summary = "库存统计信息", description = "获取当前用户的库存统计概况")
    @GetMapping("/statistics")
    public Result<InventoryStatisticsVO> getStatistics() {
        InventoryStatisticsVO statistics = inventoryService.getStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "批量查询库存", description = "根据库存ID列表批量查询库存信息")
    @PostMapping("/batch")
    public Result<List<InventoryVO>> getInventoryByIds(
            @Parameter(description = "库存ID列表")
            @RequestBody @NotEmpty(message = "库存ID列表不能为空") List<Long> ids) {
        List<InventoryVO> list = inventoryService.getInventoryByIds(ids);
        return Result.success(list);
    }
}
