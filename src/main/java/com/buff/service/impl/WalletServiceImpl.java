package com.buff.service.impl;

import com.buff.common.PageResult;
import com.buff.common.ResultCode;
import com.buff.constant.WalletLogType;
import com.buff.exception.BusinessException;
import com.buff.mapper.UserMapper;
import com.buff.mapper.WalletLogMapper;
import com.buff.model.dto.RechargeDTO;
import com.buff.model.dto.WithdrawDTO;
import com.buff.model.entity.User;
import com.buff.model.entity.WalletLog;
import com.buff.model.vo.WalletLogVO;
import com.buff.service.WalletService;
import com.buff.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 钱包服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final UserMapper userMapper;
    private final WalletLogMapper walletLogMapper;

    @Override
    public BigDecimal getBalance() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }

        return user.getBalance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(RechargeDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }

        // 2. 计算充值后余额
        BigDecimal newBalance = user.getBalance().add(dto.getAmount());

        // 3. 更新用户余额（使用乐观锁）
        int updateCount = userMapper.updateBalance(userId, newBalance, user.getVersion());
        if (updateCount == 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "充值失败，请重试");
        }

        // 4. 记录资金流水
        recordWalletLog(userId, WalletLogType.RECHARGE, dto.getAmount(), newBalance, null, "账户充值");

        log.info("用户充值成功: userId={}, amount={}, newBalance={}", userId, dto.getAmount(), newBalance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(WithdrawDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "用户不存在");
        }

        // 2. 验证余额是否足够
        if (user.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "余额不足");
        }

        // 3. 计算提现后余额
        BigDecimal newBalance = user.getBalance().subtract(dto.getAmount());

        // 4. 更新用户余额（使用乐观锁）
        int updateCount = userMapper.updateBalance(userId, newBalance, user.getVersion());
        if (updateCount == 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "提现失败，请重试");
        }

        // 5. 记录资金流水
        recordWalletLog(userId, WalletLogType.WITHDRAW, dto.getAmount().negate(), newBalance, null, "账户提现");

        log.info("用户提现成功: userId={}, amount={}, newBalance={}", userId, dto.getAmount(), newBalance);
    }

    @Override
    public PageResult<WalletLogVO> getWalletLogs(Integer type, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询总数
        Long total = walletLogMapper.countUserLogs(userId, type);

        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 查询列表
        List<WalletLogVO> list = walletLogMapper.selectUserLogs(userId, type, offset, pageSize);

        return new PageResult<>(total, list, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordWalletLog(Long userId, Integer type, BigDecimal amount, BigDecimal balanceAfter, String orderNo, String remark) {
        WalletLog walletLog = new WalletLog();
        walletLog.setUserId(userId);
        walletLog.setType(type);
        walletLog.setAmount(amount);
        walletLog.setBalanceAfter(balanceAfter);
        walletLog.setOrderNo(orderNo);
        walletLog.setRemark(remark);
        walletLog.setCreateTime(LocalDateTime.now());

        walletLogMapper.insert(walletLog);

        log.debug("记录资金流水: userId={}, type={}, amount={}, balanceAfter={}", userId, type, amount, balanceAfter);
    }
}
