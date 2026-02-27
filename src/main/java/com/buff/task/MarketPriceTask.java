package com.buff.task;

import com.buff.service.MarketPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 市场价格定时任务
 * 每天凌晨 2 点从 Steam Community Market 同步参考价格。
 *
 * @author Administrator
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketPriceTask {

    private final MarketPriceService marketPriceService;

    /**
     * 每天凌晨 2 点执行价格同步。
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncMarketPrices() {
        log.info("开始执行 Steam 市场价格同步任务");
        try {
            marketPriceService.fetchAndUpdateAllPrices();
            log.info("Steam 市场价格同步任务执行成功");
        } catch (Exception e) {
            log.error("Steam 市场价格同步任务执行失败", e);
        }
    }
}
