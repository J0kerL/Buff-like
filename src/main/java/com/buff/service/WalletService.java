package com.buff.service;

import com.buff.common.PageResult;
import com.buff.model.dto.RechargeDTO;
import com.buff.model.dto.WithdrawDTO;
import com.buff.model.vo.WalletLogVO;

import java.math.BigDecimal;

/**
 * 钱包服务接口
 *
 * @author Administrator
 */
public interface WalletService {

    /**
     * 查询余额
     */
    BigDecimal getBalance();

    /**
     * 充值
     */
    void recharge(RechargeDTO dto);

    /**
     * 提现
     */
    void withdraw(WithdrawDTO dto);

    /**
     * 查询资金流水
     */
    PageResult<WalletLogVO> getWalletLogs(Integer type, Integer pageNum, Integer pageSize);

    /**
     * 记录资金流水（内部方法）
     */
    void recordWalletLog(Long userId, Integer type, BigDecimal amount, BigDecimal balanceAfter, String orderNo, String remark);
}
