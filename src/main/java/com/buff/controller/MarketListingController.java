package com.buff.controller;

import com.buff.common.PageResult;
import com.buff.common.Result;
import com.buff.model.dto.ListingCreateDTO;
import com.buff.model.dto.MarketQueryDTO;
import com.buff.model.vo.MarketListingVO;
import com.buff.service.MarketListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 市场挂单控制器
 *
 * @author Administrator
 */
@Tag(name = "市场管理", description = "市场挂单相关接口")
@Validated
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketListingController {

    private final MarketListingService marketListingService;

    @Operation(summary = "上架商品", description = "将库存中的饰品上架到市场")
    @PostMapping("/list")
    public Result<Long> createListing(@Valid @RequestBody ListingCreateDTO dto) {
        Long listingId = marketListingService.createListing(dto);
        return Result.success(listingId);
    }

    @Operation(summary = "下架商品", description = "将已上架的商品下架")
    @DeleteMapping("/list/{id}")
    public Result<Void> cancelListing(
            @Parameter(description = "挂单ID", example = "1")
            @PathVariable Long id) {
        marketListingService.cancelListing(id);
        return Result.success();
    }

    @Operation(summary = "查询市场商品", description = "分页查询市场在售商品，支持多条件筛选和排序")
    @GetMapping("/listings")
    public Result<PageResult<MarketListingVO>> getMarketListings(@Valid MarketQueryDTO queryDTO) {
        PageResult<MarketListingVO> result = marketListingService.getMarketListings(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "查看挂单详情", description = "查看单个挂单的详细信息")
    @GetMapping("/listing/{id}")
    public Result<MarketListingVO> getListingDetail(
            @Parameter(description = "挂单ID", example = "1")
            @PathVariable Long id) {
        MarketListingVO listingVO = marketListingService.getListingDetail(id);
        return Result.success(listingVO);
    }

    @Operation(summary = "查看我的挂单", description = "查看当前用户的所有挂单")
    @GetMapping("/my-listings")
    public Result<PageResult<MarketListingVO>> getMyListings(
            @Parameter(description = "挂单状态：0=上架中, 2=已下架")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<MarketListingVO> result = marketListingService.getMyListings(status, pageNum, pageSize);
        return Result.success(result);
    }
}
