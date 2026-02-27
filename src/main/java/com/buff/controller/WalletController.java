package com.buff.controller;

import com.buff.common.PageResult;
import com.buff.common.Result;
import com.buff.model.dto.RechargeDTO;
import com.buff.model.dto.WithdrawDTO;
import com.buff.model.vo.WalletLogVO;
import com.buff.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 钱包控制器
 *
 * @author Administrator
 */
@Tag(name = "钱包管理", description = "钱包和资金流水相关接口")
@Validated
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "查询余额", description = "查询当前用户的钱包余额")
    @GetMapping("/balance")
    public Result<BigDecimal> getBalance() {
        BigDecimal balance = walletService.getBalance();
        return Result.success(balance);
    }

    @Operation(summary = "充值", description = "向钱包充值")
    @PostMapping("/recharge")
    public Result<Void> recharge(@Valid @RequestBody RechargeDTO dto) {
        walletService.recharge(dto);
        return Result.success();
    }

    @Operation(summary = "提现", description = "从钱包提现")
    @PostMapping("/withdraw")
    public Result<Void> withdraw(@Valid @RequestBody WithdrawDTO dto) {
        walletService.withdraw(dto);
        return Result.success();
    }

    @Operation(summary = "查询资金流水", description = "分页查询当前用户的资金流水记录")
    @GetMapping("/logs")
    public Result<PageResult<WalletLogVO>> getWalletLogs(
            @Parameter(description = "流水类型：1=充值, 2=提现, 3=购买支出, 4=出售收入")
            @RequestParam(required = false) Integer type,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<WalletLogVO> result = walletService.getWalletLogs(type, pageNum, pageSize);
        return Result.success(result);
    }
}
