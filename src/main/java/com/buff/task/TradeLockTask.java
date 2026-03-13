package com.buff.task;

import com.buff.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 交易锁定定时任务
 * 每小时扫描一次，将锁定时间已到期的饰品自动解锁（status: 2 → 0）
 *
 * @author Administrator
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeLockTask {

    private final InventoryMapper inventoryMapper;

    /**
     * 每小时整点执行一次
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void unlockExpiredItems() {
        log.info("开始执行交易锁定自动解锁任务");
        try {
            int count = inventoryMapper.unlockExpiredItems();
            if (count > 0) {
                log.info("交易锁定自动解锁完成，共解锁 {} 件饰品", count);
            } else {
                log.debug("暂无需要解锁的饰品");
            }
        } catch (Exception e) {
            log.error("交易锁定自动解锁任务执行失败", e);
        }
    }
}
