package com.buff.mq.consumer;

import com.buff.constant.InventoryStatus;
import com.buff.constant.ListingStatus;
import com.buff.constant.WalletLogType;
import com.buff.exception.BusinessException;
import com.buff.mapper.InventoryMapper;
import com.buff.mapper.MarketListingMapper;
import com.buff.mapper.UserMapper;
import com.buff.model.entity.User;
import com.buff.model.entity.UserInventory;
import com.buff.mq.config.RabbitMQConfig;
import com.buff.mq.message.OrderConfirmedMessage;
import com.buff.service.WalletService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 订单消息消费者
 * <p>
 * 处理买家确认收货后的异步后处理逻辑：
 *   1. 卖家余额打款（乐观锁，失败重试）
 *   2. 记录卖家资金流水
 *   3. 库存所有权转移给买家
 *   4. 挂单状态更新为已售出
 * @author Administrator
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageConsumer {

    private final UserMapper userMapper;
    private final InventoryMapper inventoryMapper;
    private final MarketListingMapper marketListingMapper;
    private final WalletService walletService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CONFIRMED_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderConfirmed(OrderConfirmedMessage message,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("收到订单确认消息: orderId={}, orderNo={}, sellerId={}, amount={}",
                message.getOrderId(), message.getOrderNo(),
                message.getSellerId(), message.getAmount());
        try {
            // 幂等检查：查询库存归属是否已变更，避免重复消费
            UserInventory inventory = inventoryMapper.selectById(message.getInventoryId());
            if (inventory == null) {
                log.warn("库存不存在，可能已被处理，跳过: inventoryId={}", message.getInventoryId());
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (message.getBuyerId().equals(inventory.getUserId())
                    && inventory.getStatus() == InventoryStatus.IN_STOCK) {
                log.warn("库存已归属买家，消息重复消费，跳过: inventoryId={}", message.getInventoryId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 1. 卖家余额打款（乐观锁重试最多 3 次）
            creditSellerBalance(message.getSellerId(), message.getAmount(), message.getOrderNo());

            // 2. 库存所有权转移给买家
            inventory.setUserId(message.getBuyerId());
            inventory.setStatus(InventoryStatus.IN_STOCK);
            inventoryMapper.updateById(inventory);

            // 3. 挂单标记为已售出
            marketListingMapper.updateStatusById(message.getListingId(), ListingStatus.SOLD);

            log.info("订单后处理完成: orderId={}", message.getOrderId());

            // 手动 ACK
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("订单确认消息处理失败: orderId={}", message.getOrderId(), e);
            // 非首次投递则直接入死信队列，避免无限重试阻塞队列
            boolean requeue = false;
            channel.basicNack(deliveryTag, false, requeue);
        }
    }

    /**
     * 向卖家账户打款，使用乐观锁，最多重试 3 次。
     */
    private void creditSellerBalance(Long sellerId, BigDecimal amount, String orderNo) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            User seller = userMapper.selectById(sellerId);
            if (seller == null) {
                throw new BusinessException(500, "卖家账户不存在: sellerId=" + sellerId);
            }
            BigDecimal newBalance = seller.getBalance().add(amount);
            int updated = userMapper.updateBalance(sellerId, newBalance, seller.getVersion());
            if (updated > 0) {
                walletService.recordWalletLog(sellerId, WalletLogType.SALE_INCOME,
                        amount, newBalance, orderNo, "出售商品收入");
                log.info("卖家余额打款成功: sellerId={}, amount={}, newBalance={}",
                        sellerId, amount, newBalance);
                return;
            }
            log.warn("卖家余额打款乐观锁冲突，第 {} 次重试: sellerId={}", i + 1, sellerId);
        }
        throw new BusinessException(500, "卖家余额打款失败，已超过最大重试次数: sellerId=" + sellerId);
    }
}
