package com.buff.service.impl;

import com.buff.common.PageResult;
import com.buff.common.ResultCode;
import com.buff.constant.InventoryStatus;
import com.buff.constant.ListingStatus;
import com.buff.constant.OrderStatus;
import com.buff.constant.WalletLogType;
import com.buff.exception.BusinessException;
import com.buff.mapper.InventoryMapper;
import com.buff.mapper.MarketListingMapper;
import com.buff.mapper.TradeOrderMapper;
import com.buff.mapper.UserMapper;
import com.buff.model.dto.OrderCreateDTO;
import com.buff.model.entity.MarketListing;
import com.buff.model.entity.TradeOrder;
import com.buff.model.entity.User;
import com.buff.model.entity.UserInventory;
import com.buff.model.vo.OrderVO;
import com.buff.service.TradeOrderService;
import com.buff.service.WalletService;
import com.buff.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 交易订单服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeOrderServiceImpl implements TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final MarketListingMapper marketListingMapper;
    private final InventoryMapper inventoryMapper;
    private final UserMapper userMapper;
    private final WalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderCreateDTO dto) {
        Long buyerId = UserContext.getUserId();
        if (buyerId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询挂单信息
        MarketListing listing = marketListingMapper.selectById(dto.getListingId());
        if (listing == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "挂单不存在");
        }

        // 2. 验证挂单状态（必须是上架中）
        if (listing.getStatus() != ListingStatus.ON_SALE) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "该商品已下架或已售出");
        }

        // 3. 验证不能购买自己的商品
        if (listing.getSellerId().equals(buyerId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "不能购买自己的商品");
        }

        // 4. 使用乐观锁更新挂单状态为已被购买
        int updateCount = marketListingMapper.updateStatus(
                dto.getListingId(),
                ListingStatus.PURCHASED,
                listing.getVersion()
        );
        if (updateCount == 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "商品已被他人购买，请选择其他商品");
        }

        // 5. 生成订单号
        String orderNo = generateOrderNo();

        // 6. 创建订单
        TradeOrder order = new TradeOrder();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(listing.getSellerId());
        order.setListingId(dto.getListingId());
        order.setInventoryId(listing.getInventoryId());
        order.setTotalAmount(listing.getPrice());
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setCreateTime(LocalDateTime.now());

        tradeOrderMapper.insert(order);

        log.info("创建订单成功: orderNo={}, buyerId={}, sellerId={}, amount={}",
                orderNo, buyerId, listing.getSellerId(), listing.getPrice());

        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id) {
        Long buyerId = UserContext.getUserId();
        if (buyerId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询订单
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该订单");
        }

        // 3. 验证订单状态（必须是待支付）
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单状态异常，无法支付");
        }

        // 4. 查询买家余额
        User buyer = userMapper.selectById(buyerId);
        if (buyer.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "余额不足，请先充值");
        }

        // 5. 扣除买家余额（使用乐观锁）
        BigDecimal newBalance = buyer.getBalance().subtract(order.getTotalAmount());
        int updateCount = userMapper.updateBalance(buyerId, newBalance, buyer.getVersion());
        if (updateCount == 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "支付失败，请重试");
        }

        // 6. 记录资金流水
        walletService.recordWalletLog(buyerId, WalletLogType.PURCHASE,
                order.getTotalAmount().negate(), newBalance, order.getOrderNo(), "购买商品");

        // 7. 更新订单状态为待发货
        tradeOrderMapper.updateStatus(id, OrderStatus.PAID_WAIT_DELIVERY);
        tradeOrderMapper.updatePayTime(id);

        log.info("订单支付成功: orderId={}, buyerId={}, amount={}", id, buyerId, order.getTotalAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(Long id) {
        Long sellerId = UserContext.getUserId();
        if (sellerId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询订单
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该订单");
        }

        // 3. 验证订单状态（必须是待发货）
        if (order.getStatus() != OrderStatus.PAID_WAIT_DELIVERY) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单状态异常，无法发货");
        }

        // 4. 更新订单状态为已发货
        tradeOrderMapper.updateStatus(id, OrderStatus.DELIVERED);
        tradeOrderMapper.updateDeliverTime(id);

        log.info("订单发货成功: orderId={}, sellerId={}", id, sellerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long id) {
        Long buyerId = UserContext.getUserId();
        if (buyerId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询订单
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该订单");
        }

        // 3. 验证订单状态（必须是已发货）
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单状态异常，无法确认收货");
        }

        // 4. 查询卖家信息
        User seller = userMapper.selectById(order.getSellerId());

        // 5. 增加卖家余额（使用乐观锁）
        BigDecimal newBalance = seller.getBalance().add(order.getTotalAmount());
        int updateCount = userMapper.updateBalance(order.getSellerId(), newBalance, seller.getVersion());
        if (updateCount == 0) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "确认收货失败，请重试");
        }

        // 6. 记录资金流水
        walletService.recordWalletLog(order.getSellerId(), WalletLogType.SALE_INCOME,
                order.getTotalAmount(), newBalance, order.getOrderNo(), "出售商品收入");

        // 7. 转移库存所有权
        UserInventory inventory = inventoryMapper.selectById(order.getInventoryId());
        inventory.setUserId(buyerId);
        inventory.setStatus(InventoryStatus.IN_STOCK);
        inventoryMapper.updateById(inventory);

        // 8. 更新挂单状态为已售出
        marketListingMapper.updateStatus(order.getListingId(), ListingStatus.SOLD, null);

        // 9. 更新订单状态为交易成功
        tradeOrderMapper.updateStatus(id, OrderStatus.SUCCESS);
        tradeOrderMapper.updateFinishTime(id);

        log.info("订单确认收货成功: orderId={}, buyerId={}, sellerId={}, amount={}",
                id, buyerId, order.getSellerId(), order.getTotalAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 1. 查询订单
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单不存在");
        }

        // 2. 验证订单所有权（买家或卖家都可以取消）
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权操作该订单");
        }

        // 3. 验证订单状态（只能取消待支付的订单）
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "该订单无法取消");
        }

        // 4. 更新订单状态为已取消
        tradeOrderMapper.updateStatus(id, OrderStatus.CANCELLED);

        // 5. 恢复挂单状态为上架中
        MarketListing listing = marketListingMapper.selectById(order.getListingId());
        marketListingMapper.updateStatus(order.getListingId(), ListingStatus.ON_SALE, listing.getVersion());

        log.info("订单取消成功: orderId={}, userId={}", id, userId);
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        OrderVO orderVO = tradeOrderMapper.selectOrderDetailById(id);
        if (orderVO == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "订单不存在");
        }

        // 验证订单所有权
        if (!orderVO.getBuyerId().equals(userId) && !orderVO.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无权查看该订单");
        }

        return orderVO;
    }

    @Override
    public PageResult<OrderVO> getMyBuyOrders(Integer status, Integer pageNum, Integer pageSize) {
        Long buyerId = UserContext.getUserId();
        if (buyerId == null) {
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
        Long total = tradeOrderMapper.countMyBuyOrders(buyerId, status);

        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 查询列表
        List<OrderVO> list = tradeOrderMapper.selectMyBuyOrders(buyerId, status, offset, pageSize);

        return new PageResult<>(total, list, pageNum, pageSize);
    }

    @Override
    public PageResult<OrderVO> getMySellOrders(Integer status, Integer pageNum, Integer pageSize) {
        Long sellerId = UserContext.getUserId();
        if (sellerId == null) {
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
        Long total = tradeOrderMapper.countMySellOrders(sellerId, status);

        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 查询列表
        List<OrderVO> list = tradeOrderMapper.selectMySellOrders(sellerId, status, offset, pageSize);

        return new PageResult<>(total, list, pageNum, pageSize);
    }

    /**
     * 生成订单号
     * 格式：yyyyMMddHHmmss + 6位随机数
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(900000) + 100000;
        return timestamp + random;
    }
}
