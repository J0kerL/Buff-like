package com.buff.controller;

import com.buff.common.PageResult;
import com.buff.common.Result;
import com.buff.model.vo.NotificationVO;
import com.buff.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 *
 * @author Administrator
 */
@Tag(name = "通知管理", description = "通知相关接口")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取未读通知数量", description = "获取当前用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long count = notificationService.getUnreadCount();
        return Result.success(count);
    }

    @Operation(summary = "获取通知列表", description = "分页获取当前用户的通知列表")
    @GetMapping("/list")
    public Result<PageResult<NotificationVO>> getList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(required = false) Integer pageNum,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(required = false) Integer pageSize) {
        PageResult<NotificationVO> result = notificationService.getList(pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "标记已读", description = "将指定通知标记为已读")
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID", example = "1")
            @PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "根据订单ID标记已读", description = "将指定订单相关的通知标记为已读")
    @PostMapping("/mark-read-by-order/{orderId}")
    public Result<Void> markAsReadByOrderId(
            @Parameter(description = "订单ID", example = "1")
            @PathVariable Long orderId) {
        notificationService.markAsReadByOrderId(orderId);
        return Result.success();
    }

    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "通知ID", example = "1")
            @PathVariable Long id) {
        notificationService.delete(id);
        return Result.success();
    }
}
