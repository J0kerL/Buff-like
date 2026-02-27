package com.buff.controller;

import com.buff.common.PageResult;
import com.buff.common.Result;
import com.buff.model.dto.OrderCreateDTO;
import com.buff.model.vo.OrderVO;
import com.buff.service.TradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 交易订单控制器
 *
 * @author Administrator
 */
@Tag(name = "订单管理", description = "交易订单相关接口")
@Validated
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @Operation(summary = "创建订单", description = "购买市场商品，创建交易订单")
    @PostMapping("/create")
    public Result<String> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        String orderNo = tradeOrderService.createOrder(dto);
        return Result.success(orderNo);
    }

    @Operation(summary = "支付订单", description = "买家支付订单，扣除余额")
    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable Long id) {
        tradeOrderService.payOrder(id);
        return Result.success();
    }

    @Operation(summary = "卖家发货", description = "卖家确认发货")
    @PostMapping("/{id}/deliver")
    public Result<Void> deliverOrder(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable Long id) {
        tradeOrderService.deliverOrder(id);
        return Result.success();
    }

    @Operation(summary = "买家确认收货", description = "买家确认收货，完成交易")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmOrder(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable Long id) {
        tradeOrderService.confirmOrder(id);
        return Result.success();
    }

    @Operation(summary = "取消订单", description = "取消待支付的订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable Long id) {
        tradeOrderService.cancelOrder(id);
        return Result.success();
    }

    @Operation(summary = "查看订单详情", description = "查看单个订单的详细信息")
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable Long id) {
        OrderVO orderVO = tradeOrderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    @Operation(summary = "查看我的购买订单", description = "查看当前用户作为买家的所有订单")
    @GetMapping("/my-buy-orders")
    public Result<PageResult<OrderVO>> getMyBuyOrders(
            @Parameter(description = "订单状态：0=待支付, 1=待发货, 2=已发货, 3=交易成功, 4=已取消")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<OrderVO> result = tradeOrderService.getMyBuyOrders(status, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "查看我的出售订单", description = "查看当前用户作为卖家的所有订单")
    @GetMapping("/my-sell-orders")
    public Result<PageResult<OrderVO>> getMySellOrders(
            @Parameter(description = "订单状态：0=待支付, 1=待发货, 2=已发货, 3=交易成功, 4=已取消")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<OrderVO> result = tradeOrderService.getMySellOrders(status, pageNum, pageSize);
        return Result.success(result);
    }
}
